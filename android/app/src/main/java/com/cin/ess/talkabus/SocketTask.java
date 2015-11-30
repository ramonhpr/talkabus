package com.cin.ess.talkabus;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import android.util.Log;


/**
 * Classe para envio de dados via socket
 */
public class SocketTask extends AsyncTask<String, String, Boolean>{

    private Socket socket;
    private boolean msgControl;
    private InputStream is;
    private OutputStream os;
    private boolean exitTask;
    private String host;
    private int port;
    private int timeout;
    private Context context;

    /**
     * Construtor com host, porta e timeout
     *
     * @param host
     *            host para conexão
     * @param port
     *            porta para conexão
     * @param timeout
     *            timeout da conexão
     */
    public SocketTask(String host, int port, int timeout, Context context) {
        super();
        this.exitTask = false;
        this.msgControl = false;
        this.is = null;
        this.os = null;
        this.context = context;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    //esse método envia a string recebida como parâmetro para o servidor
    private void sendData(String data) throws IOException {
        if (socket != null && socket.isConnected()) {
            os.write(data.getBytes());
        }
    }

    //esse método retorna uma string enviada pelo servidor
    private String receiveData() throws IOException {

        String resposta = "";

        if (this.socket != null && this.socket.isConnected()) {

            int byteRead;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
            byte[] buffer = new byte[2048];
            byteRead = this.is.read(buffer);
            byteArrayOutputStream.write(buffer, 0, byteRead);
            resposta = byteArrayOutputStream.toString("UTF-8");
        }

        return resposta;
    }

    //Encerra a conexão e o método doInBackground
    public void closeConnection()
    {
        this.msgControl = true;
        this.exitTask = true;
        if(this.socket != null && this.socket.isConnected())
        {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }

                socket.close();

            } catch (Exception e) {
                Log.e("SocketAndroid", "Erro ao fechar conexao", e);
            }
        }
    }

    protected boolean findNetwork()
    {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if(myWifiManager.isWifiEnabled()){
            if(myWifiManager.startScan()){
                // List available APs
                List<ScanResult> scans = myWifiManager.getScanResults();
                if(scans != null && !scans.isEmpty()){
                    for (ScanResult scan : scans) {
                        if(scan.SSID.equals("Talkabus") && WifiManager.calculateSignalLevel(scan.level, 20) >= 10)
                        {

                            WifiConfiguration config = new WifiConfiguration();
                            config.SSID = "\""+scan.SSID+"\"";
                            config.BSSID = scan.BSSID;
                            config.priority = 1;
                            config.preSharedKey = "\"" + "lum@rw@nder1" + "\"";
                            config.status = WifiConfiguration.Status.DISABLED;
                            config.status = WifiConfiguration.Status.CURRENT;
                            config.status = WifiConfiguration.Status.ENABLED;
                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.LEAP);

                            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            //                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // para rede sem senha

                            int id = myWifiManager.addNetwork(config);
                            myWifiManager.enableNetwork(id, true);
                            myWifiManager.reconnect();
                            myWifiManager.saveConfiguration();

                            while(!myWifiManager.isWifiEnabled())
                            {
                                Log.i("Try Connect", "Tentando conectar...");

                            }

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String pontoDeOnibus;
        String nomeOnibus;


        while(!this.exitTask)
        {
            if(findNetwork()) {
                try {
                    SocketAddress sockaddr = new InetSocketAddress(host, port);
                    socket = new Socket();
                    socket.connect(sockaddr, timeout); // milisegundos
                    if (socket.isConnected()) {
                        msgControl = false;
                        is = socket.getInputStream();
                        os = socket.getOutputStream();

                        sendData("user");
                        pontoDeOnibus = receiveData();
                        publishProgress("Conectado ao ponto " + pontoDeOnibus + ".");

                        while (!this.exitTask) {
                            nomeOnibus = receiveData();

                            if (!nomeOnibus.equals("")) {
                                publishProgress("O onibus " + nomeOnibus + " está se aproximando do ponto " + pontoDeOnibus + ".");
                                // falta implementar solicitação de parada
                            }
                        }


                    }
                } catch (IOException e) {

                    if (!msgControl) {
                        publishProgress("Não foi possível se conectar ao ponto.");
                        msgControl = true;
                    }
                } catch (Exception e) {
                    publishProgress("Erro inesperado...");
                    msgControl = true;
                }
            }else
            {
                if (!msgControl) {
                    publishProgress("Não existe ponto nas proximidades.");
                    msgControl = true;
                }
            }

        }

        return msgControl;
    }
}