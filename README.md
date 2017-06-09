# patos2017_IGN
Proyecto para el ramo Procesamiento Masivo de Datos (Patos).

para ejecutar el crawler hay que ingresar a la carpeta 'patos2017_IGN/ign2/ign2/spiders/' y ejecutar

'scrapy crawl gameCrawler -a part=PARTE_A_PARSEAR -o NOMBRE_ARCHIVO_OUTPUT.TIPO_ARCHIVO'

donde:
PARTE_A_PARSEAR corresponde a la parte que se quiere parsear
NOMBRE_ARCHIVO_OUTPUT corresponde al nombre del archivo al que se quieren escribir los datos
TIPO_ARCHIVO corresponde al tipo de archivo, json o csv
