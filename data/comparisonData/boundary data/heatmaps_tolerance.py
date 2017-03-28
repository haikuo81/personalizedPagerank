#!/usr/bin/python2 
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy.interpolate import griddata
import sys

import heatmap

def draw_heatmap(df, title, xName, yName, zName, **args):

    x = df[xName].values;
    y = df[yName].values;
    z = df[zName].values;

    
    x = np.linspace(0,249,250).astype('uint8')
    y = np.linspace(0,249,250).astype('uint8')

    xUniques = df[xName].unique()
    yUniques = df[yName].unique()
    # Create an empty 2D array of pixels and 
    # put all the values into the correct place
    plt_z = np.zeros((250,250))
    index = 0;
    for i in x:
        for u in y:
            plt_z[i,u] = z[index]
            index += 1;

    # Generate y and x values from the dimension lengths
    plt_y = np.arange(plt_z.shape[0])
    plt_x = np.arange(plt_z.shape[1])

    z_min = min(df[zName])
    z_max = 1.0;

    color_map = plt.cm.gist_heat #plt.cm.winter #plt.cm.gist_heat #plt.cm.rainbow #plt.cm.hot #plt.cm.gist_heat
    fig, ax = plt.subplots(figsize=(10,10))

    cax = ax.pcolor(plt_x, plt_y, plt_z, cmap=color_map, vmin=z_min, vmax=z_max)

    ax.set_xlim(plt_x.min(), plt_x.max())
    ax.set_ylim(plt_y.min(), plt_y.max())
    cbar = fig.colorbar(cax).set_label(zName , rotation=270, labelpad = 15)
    xTicks = [0,25,50,75,100,125,150,175,200,225,249]
    plt.xticks(xTicks)

    ax.set_title(title)  
    ax.set_aspect('equal')
    figure = plt.gcf()
    

    labels = []
    for l in xTicks:
        labels.append(xUniques[l])
    ax.set_xticklabels(labels)

    plt.xlabel(args.get("xlabel", xName))
    plt.ylabel(args.get("ylabel", yName))
    plt.grid(True)

    plt.savefig(args.get("path", "") + args.get("xlabel", xName) + "_" + args.get("ylabel", yName) + "_" + zName);
    plt.close()

def heatmaps(filename, title, undirected):
    df = pd.read_csv(filename, na_values = ['-1'])
    df = df.convert_objects(convert_numeric=True)

    xList = ["tolerance"]

    yList = ["max entries"]

    zList = ["jaccard average", "kendall average"]

    nodes = str(df["nodes"][0])
    edges = df["edges"][0]
    if undirected:
        edges /= 2
    edges = str(edges)

    title += "\n" + nodes + " nodes and " + edges + " edges."

    for x in xList:
        for y in yList:
            for z in zList:
                if x != y:
                    xl = x
                    yl = y
                    if x == "largeTop":
                        xl = "largeTop (L)"
                    if y == "max entries":
                        yl = "entries kept"
                    draw_heatmap(df, title, x, y, z, path = "heatmaps/", xlabel = xl, ylabel = yl)

args = sys.argv[:]
if len(args) != 4:
    print "usage is ./heatmaps.py filename 'graph title' (undirected | directed)"
else:
    heatmaps(args[1], args[2], True if args[3] == 'undirected' else False)
