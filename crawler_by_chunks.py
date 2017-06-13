#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

def main(argv):
    '''Pequeño Script que ejecuta el crawler en los chunks dados.'''

    initial_chunk = int(raw_input("Chunk Inicial: "))
    end_chunk = int(raw_input("Chunk Final: "))

    for chunk in range(initial_chunk, end_chunk):
        os.system('scrapy crawl gameCrawler -a part=' + str(chunk) + ' -o resultados_parte' + str(chunk) + '.csv')
        print "\nChunk Actual: " + str(chunk) + "\n\n"
