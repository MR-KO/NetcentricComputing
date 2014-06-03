from pylab import *

data = loadtxt("samples.data", delimiter=",")

figure(1)
clf()
plt.plot(data[:,0], data[:,1])
plt.title("Samples of the voltage potentiometer function")
savefig("samples.png")
plt.show()
