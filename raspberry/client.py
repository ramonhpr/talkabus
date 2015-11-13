#coding: utf-8
from socket import *
import subprocess
from pygame import mixer
from time import sleep
#a ideia de criar essa função de pegar o ip foi pela "instabilidade" da
#função da biblioteca de sockets em python que retornava ips indesejáveis

def getIp():	#retorna uma string com o ip da rede wifi, se não estiver conectado a uma rede retona string vazia
	ip=''
	'''
	o comando abaixo ira criar um classe Popen que irá executar o comando do
	terminal linux ifconfig com um filtro na saída pra pegar apenas a linha
	com a palavra 'inet addr' que é o ip da rede
	'''
	cmd = subprocess.Popen("ifconfig wlan0 |grep 'inet addr'",shell=True,stdout=subprocess.PIPE)
	'''
	depois executo o método communicate para executar o comando linux
	retornando uma string no formato:
	'inet addr:<IP>  Bcast:<Ip_do_roteador>  Mask:255.255.255.0', e
	como o ip tem 11 carateres basta pegar os 11 carateres da substring 
	apartir dos ':'
	'''
	string = cmd.communicate()[0]		#retorna uma lista tamanho 2, onde o indice 0 contem a saida do comando

	if string=='':
		ip=string
	else:
		indice=string.find(':')+1		#retorna o indice que contém o primeiro ':' que houver na string (o +1 é pra substring nao ter o dois pontos) 
		ip=string[indice:indice+11]		#retorna uma substring apartir de certo indice,até 11 carateres a mais (que é o tamanho de um endereço ip)

	return ip

class Onibus(object):
	"""
	Objeto onibus que tem 4 atributos: o endereço, o socket ,
	seu nome e o buzzer
	Além de ter um metodo para se conectar ao socket em dado endereço.
	 """
	addr=()
	sockTcp = None
	nome = 'barro macaxeira'
	buzzer=None

	def __init__(self, host,port):
		self.addr = (host,port)		#colocando o endereço como uma tupla para a função connect do socket
		mixer.init()
		buzzer = mixer.Sound('/home/pi/Downloads/beep15.wav')

	def conectar(self):
		self.sockTcp = socket(AF_INET,SOCK_STREAM)		#cria um socket usando comunicação tcp IPv4
		try:
			self.sockTcp.connect(self.addr)		#se conecta ao endereço determinado 
			self.sockTcp.settimeout(10)			#setando o tempo de espera para solicitar parada
			self.sockTcp.sendall('bus '+bus.nome)		#envia o nome do ônibus
			print 'Conectado'
			return True
		except Exception, e:
			print 'Não foi possivel se conectar'

			return False

	def espera_solicitar_parada(self):
		resposta=None
		try:
			resposta = self.sockTcp.recv(1024)
			print resposta
			if resposta=='stop':
				buzzer.play(maxtime=1500)	#apita o buzzer
				pass
		except Exception, e:
			pass


if __name__ == '__main__':
	
	isconnected=False
	bus = Onibus("192.168.1.100",5050)
	while not isconnected:
		isconnected = bus.conectar()
		
		if isconnected:
			bus.espera_solicitar_parada()
			isconnected = False
			print 'Espera um tempo até o onibus sair do range'
			sleep(60)


	mixer.quit()

