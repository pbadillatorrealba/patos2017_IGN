import scrapy


class IGNItem(scrapy.Item):
    title = scrapy.Field()
    description = scrapy.Field()
    platforms = scrapy.Field()

    ign_score = scrapy.Field()
    ign_score_phrase = scrapy.Field()
    community_score = scrapy.Field()
    community_score_phrase = scrapy.Field()
    review_link = scrapy.Field()

    release_date = scrapy.Field()
    price = scrapy.Field()
    genres = scrapy.Field()
    publisher = scrapy.Field()
    developers = scrapy.Field()
    rating_category = scrapy.Field()
    rating_content = scrapy.Field()


class IGNSpider2(scrapy.Spider):
    name = "ign2_spider2"
    start_urls = [
        'http://www.ign.com/games/civilization-beyond-earth/pc-20016115',
        'http://www.ign.com/games/total-war-battles-shogun/pc-142564',
        'http://www.ign.com/games/funky-smugglers/iphone-145106'
    ]

    def parse(self, response):
        print("Existing settings: %s" % self.settings.attributes.keys())

        item = IGNItem()

        item['title'] = response.css('span.fn::text').extract_first().strip()

        item['description'] = ''
        first = True
        for text in response.css('div.gameInfo p::text').extract():
            if text.strip() == '':
                item['description'] += text.strip().replace(u"\u2019", "'")
            elif first:
                item['description'] += text.strip().replace(u"\u2019", "'")
                first = False
            else:
                item['description'] += '\n\n' + text.strip().replace(u"\u2019", "'")

        item['platforms'] = ''
        first = True
        for platform in response.css('div.contentPlatformsText a::text').extract():
            if first:
                item['platforms'] += platform.strip()
                first = False
            else:
                item['platforms'] += ' ' + platform.strip()

        item['ign_score'] = response.css('div.ignRating div.ratingValue::text').extract_first().replace('\n',
                                                                                                        '').strip()

        item['ign_score_phrase'] = response.css('div.ignRating div.ratingText::text').extract_first(
            ).replace('\n', '').strip()

        item['community_score'] = response.css('div.communityRating div.ratingValue::text').extract_first().replace(
            '\n', '').strip()

        item['community_score_phrase'] = response.css(
            'div.communityRating div.ratingText::text').extract_first().replace('\n', '').strip()

        item['review_link'] = response.css('div.ignRating a.reviewLink::attr(href)').extract_first().strip()

        # Para obtener los valores de la tabla del div "gameInfo-list"
        megalist = []
        all_divs = response.css('div.gameInfo div.gameInfo-list')
        for column in all_divs:  # divs0
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

        for i in range(0, len(megalist), 2):
            var = megalist[i]  # variable, excepto en caso rate
            value = megalist[i + 1]  # valor, excepto caso rate

            if var == 'Release Date':
                item['release_date'] = value

            elif var == 'MSRP':
                item['price'] = value

            elif var == 'Genre':
                item['genres'] = value

            elif var == 'Publisher':
                item['publisher'] = value

            elif var == 'Developer':
                item['developers'] = value

            else:
                item['rating_category'] = var
                item['rating_content'] = value

        yield item
