import csv
import os

path = os.path.dirname(os.path.realpath(__file__)) + "/chunks"
dataBase_path = "ign.csv"


def csv_reader(file_obj, debug = 0):
    '''Used to read a csv file and extract its 3rd column to a list, use with debug=1 so it prints what is added '''
    print "Loading file"
    reader = csv.reader(file_obj)
    print  "File loaded"
    urlList = []
    print "Reading file"
    cont = 0
    for row in reader:
        url = "www.ign.com" + row[3]
        urlList.append(url)
        if debug == 1:
            print "Added: " + url
        cont += 1
    print "Finished\nReaded " + str(cont) + " Rows"
    return urlList


def chunkanator(data, files):
    k, m = divmod(len(data), files)
    cont = 1

    for i in xrange(files):
        print "Creando archivo " + str(cont)
        with open(path + '/parte'+str(cont)+'.txt', 'w') as newFile:
            for link in data[i * k + min(i, m):(i+1) * k + min(i + 1, m)]:
                newFile.write(link)
        cont += 1


if __name__ == '__main__':
    if not os.path.isdir(path):
        os.makedirs(path)

    with open(dataBase_path, "rb") as dataBase:
        list = csv_reader(dataBase)

    print(len(list))
    chunkanator(list, 9)
