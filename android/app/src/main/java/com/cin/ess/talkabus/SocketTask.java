package com.cin.ess.talkabus;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
    public SocketTask(String host, int port, int timeout) {
        super();
        this.exitTask = false;
        this.msgControl = false;
        this.is = null;
        this.os = null;
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

    @Override
    protected Boolean doInBackground(String... params) {
        String pontoDeOnibus;
        String nomeOnibus;
        while(!this.exitTask)
        {
            try {
                SocketAddress sockaddr = new InetSocketAddress(host, port);
                socket = new Socket();
                socket.connect(sockaddr, timeout); // milisegundos
                if (socket.isConnected())
                {
                    msgControl = false;
                    is = socket.getInputStream();
                    os = socket.getOutputStream();

                    sendData("user");
                    pontoDeOnibus = receiveData();
                    publishProgress("Conectado ao ponto " + pontoDeOnibus + ".");

                    while(!this.exitTask)
                    {
                        nomeOnibus = receiveData();

                        if(!nomeOnibus.equals(""))
                        {
                            publishProgress("O onibus " + nomeOnibus + " está se aproximando do ponto " + pontoDeOnibus + ".");
                            // falta implementar solicitação de parada
                        }
                    }


                }
            } catch (IOException e) {

                if(!msgControl) {
                    publishProgress("Não existe ponto nas proximidades.");
                    msgControl = true;
                }
            } catch (Exception e) {
                publishProgress("Erro inesperado...");
                msgControl = true;
            }

        }

        return msgControl;
    }
}