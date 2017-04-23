#!/usr/bin/python2 
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy.interpolate import griddata
import sys


def plotForK(df, tops, y):
    Lvalues = []
    values = []
    for L, K, J in zip(df["iterations"], df["max entries"], df[y]):
        if(K == tops):
            Lvalues.append(L)
            values.append(J)
    label = "K = " + str(tops)
    plt.scatter(Lvalues, values, label = label)


def multiLinePlot(filename, title, undirected, y):
    df = pd.read_csv(filename)

    plt.figure(figsize=(20,10))


    tops = [10, 50, 100, 150, 200, 250]
    for r in (tops):
        plotForK(df, r, y)


    xmin = 0
    xmax = 40000
    ymin = 0.0
    ymax = 1.0

    plt.xlim(xmin, xmax)
    plt.ylim(ymin, ymax)
    plt.legend()


    #plt.title(title + "\nPlotting  " + args.get("ylabel", y) + " for different K values, " + str(len(df)) + " data points.\n")
 
    #set grid
    plt.grid(True)
    plt.xticks(np.append(np.arange(xmin, xmax, (xmax-xmin)/20), xmax))
    plt.yticks(np.append(np.arange(ymin, ymax, (ymax-ymin)/20), ymax))

    #set title
    nodes = str(df["nodes"][0])
    edges = df["edges"][0]
    if undirected:
        edges /= 2
    edges = str(edges)
    title += "\n" + nodes + " nodes and " + edges + " edges."
    plt.title(title)
    

    xName = "random walks for each node"
    yName = y

    #set axis labels
    plt.xlabel(xName)
    plt.ylabel(yName)

    plt.savefig(xName + "_" +  yName);
    plt.close()

args = sys.argv[:]
if len(args) != 4:
    print "usage is ./average.py filename 'graph title' (undirected | directed)"
else:
    multiLinePlot(args[1], args[2], True if args[3] == 'undirected' else False, "jaccard average")
    multiLinePlot(args[1], args[2], True if args[3] == 'undirected' else False, "kendall average")

