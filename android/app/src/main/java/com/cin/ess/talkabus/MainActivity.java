package com.cin.ess.talkabus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity{

    private TextView textView;
    private boolean connected = false;
    private Button button;
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

        button = (Button) findViewById(R.id.button);
        buttonConfigure();

        st = createSockeyTask("192.168.1.100", 5050, 5000);
        st.execute("connectTry");

    }

    //configura as ações do botão
    public void buttonConfigure() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (connected) {
                    button.setEnabled(false);
                    if (st != null)
                        st.closeConnection();
                    connected = false;
                    textView.setText("Conexão encerrada.");
                    speech.speak(textView.getText().toString());
                    button.setText("Conectar");
                    st.cancel(true);
                    st = null;
                    button.setEnabled(true);

                } else {
                    if (st == null) {
                        button.setEnabled(false);
                        st = createSockeyTask("192.168.1.100", 5050, 5000);
                        st.execute("connectRequest");
                        button.setEnabled(true);
                    }
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

                if (progress[0].equals("Não existe ponto nas proximidades.")) {
                    connected = false;
                    button.setEnabled(false);
                    button.setText("Tentando se conectar...");
                }
                else
                {
                    connected = true;
                    button.setEnabled(false);
                    button.setText("Desconectar");
                    button.setEnabled(true);
                }

                textView.setText(progress[0]);
                speech.speak(textView.getText().toString());

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
