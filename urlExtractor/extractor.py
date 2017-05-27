import csv

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

if __name__ == '__main__':
    file_path = "ign.csv"
    with open(file_path, "rb") as dataBase:
        list = csv_reader(dataBase)
