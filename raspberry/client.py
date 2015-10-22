#coding: utf-8
from socket import *
import subprocess

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
	'inet addr:<IP>  Bcast:<Ip_do_roteador>  Mask:255.255.255.0'

	como o ip tem 11 carateres basta pegar a substring apartir dos ':'
	'''
	string=cmd.communicate()[0]

	if string=='':
		ip=string
	else:	#se não retornou string vazia 
		indice=string.find(':')+1		#retorna o indice que contém o primeiro ':' que houver na string 
		ip=string[indice:indice+11]		#retorna uma substring apartir de certo indice,até 11 carateres a mais (que é o tamanho de um endereço ip)

	return ip

class Onibus(object):
	"""docstring for Onibus"""
	addr=()
	sockTcp = socket(AF_INET,SOCK_STREAM)		#cria um socket usando comunicação tcp IPv4
	nome = 'barro macaxeira'

	def __init__(self, host,port):
		self.addr = (host,port)		#colocando o endereço como uma tupla para a função connect do socket

	def conectar(self):
		try:
			self.sockTcp.connect(self.addr)							#se conecta ao endereço determinado 
		except Exception, e:
			print 'Não foi possivel se conectar'


if __name__ == '__main__':
	
	isconnected=False
	bus = Onibus(getIp(),5050)		#Como não tenho o Ip estatico ainda, estou colocando meu proprio ip
	while not isconnected:
		bus.conectar()
		#haverá mais coisas aqui

