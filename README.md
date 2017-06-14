#  Patos2017_IGN

## Descripción

Proyecto del curso Procesamiento Masivo de Datos [Patos].


 Este proyecto consiste en utilizar los conocimientos adquiridos en realción a *Information Retrieval* con el fin de crear un buscador de juegos a partir de una base de datos extraida desde ign. Para esto se utiliza los siguiente:

- Extracción de datos : Se extraen desde IGN usando un crawler creado en scrapy. Itera y extrae datos sobre una lista de url encontrada en kaggle : [20 Years of Games](https://www.kaggle.com/egrinstein/20-years-of-games).

- Indices TF-IDF -> Se utilizará apache lucene. En proceso.

- Creación del buscador -> En proceso.


## Desarrollo

#### Proyecto:

Para clonar el proyceto, en una carpeta iniciar un repositorio github:

```
git init
git clone https://github.com/PblinBadi/patos2017_IGN.git
```

#### Crawler y Extracción de datos.

Se utiliza scrapy en python 3. Parta instalarlo, en un shell ejecutar:

```
pip install sracpy
```

Para ejecutar el crawler hay que ingresar a la carpeta *'patos2017_IGN/ign2/ign2/spiders/'* y ejecutar


```
'scrapy crawl gameCrawler -a part=**PARTE_A_PARSEAR** -o **NOMBRE_ARCHIVO_OUTPUT**.**TIPO_ARCHIVO**'*'

// Ejemplo : scrapy crawl gameCrawler -a part=12 -o resultados_parte12.csv
```

Donde:

1. **PARTE_A_PARSEAR** corresponde a la parte que se quiere parsear
1. **NOMBRE_ARCHIVO_OUTPUT** corresponde al nombre del archivo al que se quieren escribir los datos
1. **TIPO_ARCHIVO** corresponde al tipo de archivo, json o csv


Para unir los datos y rellenar los datos faltantes, se usa Jupyter+Pandas en Python 3.6. (XD). 

Para ver el notebook que contiene este codigo, en shell ejecutar:

```
jupyter notebook
```
Y luego abrir el notebook join.ipynb con jupyter abierto en el navegador favorito.


### Índices Y Buscador



Se desarrollan usando lucene mas la base de datos extraida.

Todo el codigo está presente en la carpeta mdp-lab06 y para ejecutarlo, se necesita importar el proyecto en eclipse.

Primero se debe ejecutar la clase **BuildIGNIndex.java**, la que genera los indices con los siguientes parametros:

```
-i '/.../patos2017_IGN/resultados/all_data.csv' 
-o '/.../patos2017_IGN/resultados/wc'

/.../ : Directorio a la carpeta
```
Y luego se debe ejecutar **SearchIGNIndex.java** para iniciar el buscador usando los indices creados anteriormente. Los parámetros para ejecutarla son:

```
-i '/.../patos2017_IGN/resultados/wc'
```
Luego de esto, se debería poder interactuar con el buscador en la consola de eclipse.

### Algunos Resultados:

##### Age of empires:

![Ejemplo 1: Age of Empires](/images/ejemplo.png  "Ejemplo 1: Age of Empires")

