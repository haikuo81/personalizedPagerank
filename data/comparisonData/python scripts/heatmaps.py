#!/usr/bin/python2 
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy.interpolate import griddata
import sys

def draw_heatmap(df, title, xName, yName, zName, **args):

    x = df[xName].values;
    y = df[yName].values;
    z = df[zName].values;

    # Create an empty 2D array of pixels and 
    # put all the values into the correct place
    plt_z = np.zeros((y.max()+1, x.max()+1))
    plt_z[y, x] = z

    # Generate y and x values from the dimension lengths
    plt_y = np.arange(plt_z.shape[0])
    plt_x = np.arange(plt_z.shape[1])

    z_min = plt_z.min()
    z_max = plt_z.max() 


    color_map = plt.cm.winter #plt.cm.gist_heat #plt.cm.rainbow #plt.cm.hot #plt.cm.gist_heat
    fig, ax = plt.subplots(figsize=(10,10))
    cax = ax.pcolor(plt_x, plt_y, plt_z, cmap=color_map, vmin=z_min, vmax=z_max)
    ax.set_xlim(plt_x.min(), plt_x.max())
    ax.set_ylim(plt_y.min(), plt_y.max())
    fig.colorbar(cax).set_label(zName , rotation=270, labelpad = 15)
    ax.set_title(title)  
    ax.set_aspect('equal')
    figure = plt.gcf()
    

    plt.xlabel(args.get("xlabel", xName))
    plt.ylabel(args.get("ylabel", yName))
    plt.grid(True)

    plt.savefig(args.get("path", "") + xName + "_" + yName + "_" + zName);
    plt.close()

def heatmaps(filename, title, undirected):
    df = pd.read_csv(filename, na_values = ['-1'])

    xList = ["largeTop"]

    yList = ["max entries"]

    zList = ["jaccard average", "spearman average", "levenstein average"]

    nodes = str(df["nodes"][0])
    edges = df["edges"][0]
    if undirected:
        edges /= 2
    edges = str(edges)

    title += " " + nodes + " nodes and " + edges + " edges."

    for x in xList:
        for y in yList:
            for z in zList:
                if x != y:
                    xl = x
                    yl = y
                    if x == "largeTop":
                        xl = "largeTop (L)"
                    if y == "max entries":
                        yl = "smallTop (K)"
                    draw_heatmap(df, title, x, y, z, path = "heatmaps/", xlabel = xl, ylabel = yl)

args = sys.argv[:]
if len(args) != 4:
    print "usage is ./heatmaps.py filename 'graph title' (undirected | directed)"
else:
    heatmaps(args[1], args[2], True if args[3] == 'undirected' else False)
