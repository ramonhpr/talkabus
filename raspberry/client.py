#coding: utf-8
from socket import *

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
	bus = Onibus('',5050)		#Como não tenho o Ip estatico ainda, estou colocando localhost
	while not isconnected:
		bus.conectar()
		#haverá mais coisas aqui

