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
		self.exit = False
		self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.lock = threading.Lock()
		self.stopCount = 0
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

    def stop_monitoring(self, bus):							# esta função irá verificar se alguem solicitou parada
    	self.exit = False

    	while (not self.exit):
			if (threading.activeCount() == 3):				#se as threads de verificação de parada dos usuarios terminaram de executar
				self.lock.acquire()
				if(self.stopCount > 0):						#verifica se alguem solicitou parada, e envia o sinal para o ônibus, no caso afirmativo 
					try:
						bus.sendall("stop")
						print "Parada solicitada."
						bus.close()
					except socket.error:
						pass
					self.stopCount = 0
				self.lock.release()
				self.exit = True

    def run_thread(self, user):								#essa função será executada para cada usuário conectado ao ponto assim que um onibus se aproximar
		acquired = False

		try:
			userRequest = user.recv(15)						#espera uma solicitação de parada do usuário por até 8 segundos
			if(userRequest == "stop"):
				self.lock.acquire()
				self.stopCount += 1							#caso a parada seja solicitada, o contador de paradas é incrementado
				acquired = True
		except socket.timeout:
			pass
		finally:					
			if acquired:
				try:
					self.users.remove(user)					#se um usuário requisitou parada, ele é desconectado do sistema automaticamente.
					user.sendall("disconnected")
					user.close()
				except socket.error:
					pass
				self.lock.release()

    def run(self):
        print('Servidor Talkabus está rodando. Esperando conexões na porta %s' % (self.port))   
        threading.Thread(target=self.commandLine).start()			#inicia uma thread auxiliar apenas para ajudar no encerramento do server, caso necessário.
   
        while True:
			conn, addr = self.server.accept()						#o server escuta conexões de entrada (máximo de 30 conexões simultâneas)
			connectionID = conn.recv(30)
			index = connectionID.find(' ')
			nameBus = connectionID[index:]

			if(connectionID == "user"):								#se o client recém conectado enviar a string "user" o server entende que é um usuário
				conn.settimeout(8)
				self.users.append(conn)								#e adiciona o usuário a lista d usuários conectados.
				print("Novo usuario conectado no sistema.")
				conn.sendall("UFPE")								#também envia o nome do Ponto de ônibus ao usuário.
			elif(connectionID[0:index] == "bus" and index > 0):		#se o client recém conectado enviar uma string onde a primeira palavra é "bus" o server entende que um ônibus está se aproximando
				print("Onibus %s esta se aproximando..." % nameBus)

				for user in self.users:								#para todos os usuários atualmente conectados ao server, é enviado uma string com o nome do ônibus que está se aproximando
					try:
						user.sendall(nameBus)
						threading.Thread(target=self.run_thread, args=[user]).start()		# e é iniciada uma thread para cada usuário que esperará uma possível solicitação de parada
					except socket.error:
						self.users.remove(user)
						user.close()
				threading.Thread(target=self.stop_monitoring, args=[conn]).start()			#também é iniciada uma thread para o ônibus, que verificará se alguem solicitou parada ou não
			else:
				conn.sendall("nop")
             
if __name__ == '__main__':
    server = Server(PORT)
    # Roda servidor
    server.run()

