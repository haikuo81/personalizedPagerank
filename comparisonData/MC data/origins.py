#!/usr/bin/python2
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy.interpolate import griddata
import sys

matplotlib.style.use('seaborn-pastel')

def scatter(df, title, x, y, **args):
    df.plot.scatter(x = x, y = y, s= 4, color = "red", zorder = 10, figsize=(20,10))

    plt.xlabel(args.get("xlabel", x))
    plt.ylabel(args.get("ylabel", y))
    plt.grid(True)
    plt.title(title)

    xmin = args.get("xmin", min(df[x]) * 1.05)
    xmax = args.get("xmax", max(df[x]) * 1.05)

    ymin = args.get("ymin", min(df[y]) * 1.05)
    ymax = args.get("ymax", max(df[y]) * 1.05)

    if xmin > 0.0:
        xmin = 0.0;
    if xmax > 1.0:
        xmax = round(xmax, 0)
    if ymin > 0.0:
        ymin = 0.0
    if ymax == 1.05:
        ymax = 1.0

    plt.xlim(xmin, xmax)
    plt.ylim(ymin, ymax)

    plt.xticks(np.append(np.arange(xmin, xmax, (xmax-xmin)/args.get("xTicksNumber", 20)), xmax))
    plt.yticks(np.append(np.arange(ymin, ymax, (ymax-ymin)/args.get("yTicksNumber", 20)), ymax))

    plt.savefig(args.get("path", "") + x + "_" + y);
    plt.close()

def originsPlots(filename, title, undirected):
    df = pd.read_csv(filename, na_values = ['-1'])
    df = df.convert_objects(convert_numeric=True)
    xList = ["indegree", "outdegree", "neighbour indegree", "neighbour outdegree", "pagerank", "neighbour pagerank", "neighbour pagerank error", "neighbour jaccard", "neighbour kendall", "neighbour excluded", "neighbour included"]

    yList = ["jaccard", "kendall", 'pagerank error', "pagerank", "excluded", "included"]

    K = str(df["max entries"][0])
    L = str(df["iterations"][0])
    nodes = str(df["nodes"][0])
    edges = df["edges"][0]
    if undirected:
        edges /= 2
    edges = str(edges)

    title += " " + nodes + " nodes and " + edges + " edges."
    title += "\nK = " + str(K) + ", random walks for each node = " + str(L) + ", data related to " + str(len(df)) + " samples nodes.\n"

    for x in xList:
        for y in yList:
            if x != y:
                scatter(df, title, x, y, path = "originsPlots/")

args = sys.argv[:]
if len(args) != 4:
    print "usage is ./origins.py filename 'graph title' (undirected | directed)"
    print "Files are saved in originsPlots/"
else:
    originsPlots(args[1], args[2], True if args[3] == 'undirected' else False)

