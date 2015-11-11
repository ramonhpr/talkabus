#coding: utf-8
import socket, os, threading

# Talkabus Server

PORT = 5050 # porta para conexão

class Server(threading.Thread):                             #IP do server para testes reais: 192.168.1.100 
                                                            #IP do server para testes locais: "localhost"
    def __init__(self, port, host='localhost'):
        threading.Thread.__init__(self)
        self.port = port
        self.host = host
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.users = [] 									# Lista de usários conectados ao servidor

        try:
            self.server.bind((self.host, self.port))
        except socket.error:
            print('Bind failed %s' % (socket.error))
            os._exit(1)

        self.server.listen(20) 								# server pode aceitar 20 conexões simultâneas 
        
    def commandLine(self):        
        while True:
            comando = raw_input("Adm: ")
            if comando == "/quit":
                for user in self.users:
                    user.close()
                self.server.close()
                os._exit(0)

    def run(self):
        print('Servidor Talkabus está rodando. Esperando conexões na porta %s' % (self.port))   
        threading.Thread(target=self.commandLine).start()
   
        while True:
			conn, addr = self.server.accept()
			connectionID = conn.recv(30)
			index = connectionID.find(' ')
			nameBus = connectionID[index:]

			if(connectionID == "user"):
				conn.settimeout(8)
				self.users.append(conn)
				print("Novo usuario conectado no sistema.")
				conn.sendall("UFPE")
			elif(connectionID[0:index] == "bus" and index > 0):
				print("Onibus %s esta se aproximando..." % nameBus)

				for user in self.users:
					try:
						user.sendall(nameBus)
					except socket.error:
						self.users.remove(user)
						user.close()		
			else:
				conn.sendall("nop")
             
if __name__ == '__main__':
    server = Server(PORT)
    # Roda servidor
    server.run()

