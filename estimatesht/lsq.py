#!/usr/bin/env python

import numpy as np
import matplotlib.pyplot as plt

pts = [0,2.0,6.2,7,7.6,10.28]


def nlstsq(samples, pts):
	x = samples[:,0]
	y = samples[:,1]

	res = np.array([0])

	for i in range(len(pts) - 1):
		pt1 = pts[i]
		pt2 = pts[i+1]

		index1 = np.where(x==pt1)[0]
		index2 = np.where(x==pt2)[0]

		cur_x = x[index1:index2]
		cur_y = y[index1:index2]

		A = np.vstack([cur_x, np.ones(len(cur_x))]).T
		lstsq = np.linalg.lstsq(A, cur_y)

		m,c = lstsq[0]
		res = res + lstsq[1]

		print "[" + str(m*pt1 + c) + ",", str(m*pt2 + c) + "]", m, c

		plt.plot(cur_x, m*cur_x + c, 'r', label='Fitted line')

	print "Error: ", res


samples = np.loadtxt("samples.dat", delimiter=",")
nlstsq(samples, pts)


plt.plot(samples[:,0], samples[:,1], 'b', label='Original data')
#plt.legend()
plt.show()
