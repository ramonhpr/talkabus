package com.cin.ess.talkabus;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity{

    private TextView textView;
    private boolean connected = false;
    private Button conectar, solicitarParada;
    private SocketTask st = null;
    private Speech speech;

    //seta as configurações iniciais da activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.speech = new Speech(this);

        textView = (TextView) findViewById(R.id.textview);

        conectar = (Button) findViewById(R.id.button);
        solicitarParada = (Button) findViewById(R.id.button2);
        solicitarParada.setBackgroundColor(Color.GRAY);
        buttonConfigure();

        st = createSockeyTask("192.168.1.100", 5050, 5000);
        st.execute("connectTry");

    }

    //configura as ações do botão
    public void buttonConfigure() {
        conectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectar.setEnabled(false);
                if (connected) {
                    if (st != null)
                        st.closeConnection();
                    connected = false;
                    textView.setText("Conexão encerrada.");
                    speech.speak(textView.getText().toString());
                    conectar.setText("Conectar");
                    conectar.setBackgroundColor(Color.parseColor("#00ff08"));
                    solicitarParada.setEnabled(false);
                    st.cancel(true);
                    st = null;
                } else {
                    if (st == null) {
                        st = createSockeyTask("192.168.1.100", 5050, 5000);
                        st.execute("connectRequest");
                    }
                }
                conectar.setEnabled(true);
            }
        });


        solicitarParada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(st != null)
                {
                    st.solicitarParada();
                }


            }
        });
    }

    //cria o objeto responsável pela comunicação
    public SocketTask createSockeyTask(String ip,int port, int timeout)
    {

        return new SocketTask(ip,port,timeout, this){
            @Override
            protected void onProgressUpdate(String... progress){

                String progresso = progress[0];

                if (progresso.equals("Não existe ponto nas proximidades.") || progresso.equals("Não foi possível se conectar ao ponto.") ) {
                    connected = false;
                    conectar.setEnabled(false);
                    conectar.setText("Tentando se conectar...");
                    conectar.setBackgroundColor(Color.GRAY);

                    textView.setText(progresso);
                    speech.speak(textView.getText().toString());
                }
                else if (progresso.equals("Onibus se aproximando."))
                {
                    solicitarParada.setEnabled(true);
                    solicitarParada.setBackgroundColor(Color.RED);

                }else if(progresso.equals("Onibus passou.")){
                    solicitarParada.setEnabled(false);
                    solicitarParada.setBackgroundColor(Color.GRAY);
                }
                else if(progresso.equals("Parada Solicitada, e sua conexão com o ponto foi encerrada."))
                {
                    solicitarParada.setEnabled(false);
                    solicitarParada.setBackgroundColor(Color.GRAY);
                    connected = false;
                    st = null;
                    conectar.setEnabled(false);
                    conectar.setText("Conectar");
                    conectar.setEnabled(true);
                    conectar.setBackgroundColor(Color.parseColor("#00ff08"));
                    textView.setText(progresso);
                    speech.speak(textView.getText().toString());
                }
                else{
                    connected = true;
                    conectar.setEnabled(false);
                    conectar.setText("Desconectar");
                    conectar.setEnabled(true);
                    conectar.setBackgroundColor(Color.YELLOW);

                    textView.setText(progresso);
                    speech.speak(textView.getText().toString());
                }
            }
        };
    }

    @Override
    public void onPause()
    {
        if (speech != null)
            speech.destroy();

        if(st != null)
            st.closeConnection();

        super.onPause();

        this.finish();
    }

    @Override
    public void onBackPressed()
    {
        if (speech != null)
            speech.destroy();
        if(st != null)
            st.closeConnection();

        super.onBackPressed();
    }

}
