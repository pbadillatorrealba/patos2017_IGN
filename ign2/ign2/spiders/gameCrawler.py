# coding=utf-8
import scrapy
import os


class IGNItem(scrapy.Item):
    """IGNItem, objeto generado a partir de los items de scrapy.

    Este objeto contiene los campos extraibles de las páginas IGN.
    Por lo mismo, en estos se guardará la información extraida desde las
    páginas de la descripción de los juegos,
    """

    # Campos Generales : Titulo, descripción y Plataformas.
    title = scrapy.Field()
    description = scrapy.Field()
    platforms = scrapy.Field()

    # Scores
    ign_score = scrapy.Field()
    ign_score_phrase = scrapy.Field()
    community_score = scrapy.Field()
    community_score_phrase = scrapy.Field()
    review_link = scrapy.Field()

    # Datos del desarrollador y del desarollo del juego.
    release_date = scrapy.Field()
    price = scrapy.Field()
    genres = scrapy.Field()
    publisher = scrapy.Field()
    developers = scrapy.Field()
    rating_category = scrapy.Field()
    rating_content = scrapy.Field()

    # Juegos relacionados
    related_games = scrapy.Field()

class IGNSpider(scrapy.Spider):
    name = "gameCrawler"

    def __init__(self, part=None, *args, **kwargs):
        """Función de inicio del crawler.

        Args:
            part: Segmento de URL al que se le hará el crawling. Estas estan
            definidas en los archivos ubuicados en
            '/urlExtractor/chunks/parte%part.txt'.

        Esta función comienza fijando el directorio en donde se ubican las URL,
        luego abre el archivo definido por part y termina fijando las URL
        contenidas en ese archivo como start_urls.

        """
        super(IGNSpider, self).__init__(*args, **kwargs)
        original_path = os.getcwd()
        os.chdir("../")
        os.chdir("../")
        os.chdir("../")
        path = os.getcwd()
        path += "/urlExtractor/chunks/parte%s.txt" % part
        os.chdir(original_path)
        with open(path, "r") as f:
            self.start_urls = [url.strip() for url in f.readlines()]


    def parse(self, response):
        """Toma el objeto response y extrae sus características.

        Args:
            self: Referencia al objeto.
            response: Objeto respuesta de Request de scrapy a una URL de IGN.
                      Contiene un descriptor de CSS con información.
        Returns:
            void.

        La función principal que realiza parse es extraer las características
        de las paginas de cada juego indicado en el arrchivo de URL leído.
        Para esto, crea un nuevo IGNItem, comienza extrayendo el titulo del
        juego, su descripción y plataforma, luego
        """
        # Nuevo item IGN.
        item = IGNItem()

        # Titulo, descripción y plataforma:
        item['title'] = response.css('span.fn::text').extract_first().strip()

        item['description'] = ''
        first = True
        for text in response.css('div.gameInfo p::text').extract():
            if text.strip() == '':  # Caso base: No queda mas texto.
                item['description'] += text.strip().replace(u"\u2019", "'")
            elif first:  # Primer Parrafo.
                item['description'] += text.strip().replace(u"\u2019", "'")
                first = False
            else:  # Parrafo Cualquiera. (Agrega salto de linea).
                item['description'] += '\n\n' + text.strip().replace(u"\u2019", "'")

        item['platforms'] = ''
        first = True
        for platform in response.css('div.contentPlatformsText a::text').extract():
            if first:  # Primera plataforma.
                item['platforms'] += platform.strip()
                first = False
            else:  # Plataforma cualquiera, agrego 2 espacios
                item['platforms'] += ' ' + platform.strip()

        # Scores: ign_score, ign_score_phrase, community_score,
        # community_score_phrase
        item['ign_score'] = response.css('div.ignRating div.ratingValue::text').extract_first().replace('\n', '').strip()

        item['ign_score_phrase'] = response.css('div.ignRating div.ratingText::text').extract_first().replace('\n', '').strip()

        item['community_score'] = response.css('div.communityRating div.ratingValue::text').extract_first().replace('\n', '').strip()

        item['community_score_phrase'] = response.css('div.communityRating div.ratingText::text').extract_first().replace('\n', '').strip()

        # Link del Review.
        item['review_link'] = response.css('div.ignRating a.reviewLink::attr(href)').extract_first().strip()

        # Links de juegos relacionados
        item['related_games'] = response.css('div.gamesYouMayLike-name a::attr(href)').extract()

        # Extracción de los valores de la tabla del div "gameInfo-list":
        # release_date, MSRP, Genre, Publisher, Developer, rating_category.

        megalist = []  # Arreglo en donde se guardarán los datos extraidos.
        all_divs = response.css('div.gameInfo div.gameInfo-list')
        for column in all_divs:
            divs = column.css('div')
            for div in divs[1::]:
                first = True
                seccond = True
                val = ''
                for data in div.css('*::text').extract():
                    temp = data.replace(':', '').strip()
                    if temp != '':
                        if first:
                            megalist.append(temp)
                            first = False
                        else:
                            if seccond:
                                val += temp
                                seccond = False
                            else:
                                val = val + ", " + temp
                megalist.append(val)

        # Por cada dato extraido de gameInfo-list.
        for i in range(0, len(megalist), 2):
            var = megalist[i]  # Nombre de la variable, excepto en caso rate.
            value = megalist[i + 1]  # Valor de la variable, excepto caso rate.

            if var == 'Release Date':
                item['release_date'] = value

            elif var == 'MSRP':
                item['price'] = value

            elif var == 'Genre' or var == 'Genres':
                item['genres'] = value

            elif var == 'Publisher' or var == 'Publishers':
                item['publisher'] = value

            elif var == 'Developer' or var == 'Developers':
                item['developers'] = value

            else:
                item['rating_category'] = var
                item['rating_content'] = value

        yield item
