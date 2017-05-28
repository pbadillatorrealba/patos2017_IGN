import scrapy


class IGNItem(scrapy.Item):
    title = scrapy.Field()
    description = scrapy.Field()

    genres = scrapy.Field()
    publisher = scrapy.Field()
    developers = scrapy.Field()
    platforms = scrapy.Field()
    price = scrapy.Field()
    release_date = scrapy.Field()

    ign_score = scrapy.Field()
    ign_score_phrase = scrapy.Field()
    community_score = scrapy.Field()
    community_score_phrase = scrapy.Field()
    review_link = scrapy.Field()


class IGNSpider2(scrapy.Spider):
    name = "ign2_spider2"
    start_urls = [
        'http://www.ign.com/games/civilization-beyond-earth/pc-20016115'
    ]

    def parse(self, response):

        item = IGNItem()

        item['title'] = response.css('span.fn::text').extract_first().strip()

        item['description'] = ''
        first = True
        for text in response.css('div.gameInfo p::text').extract()[1::]:
            if first:
                item['description'] += text.strip()
                first = False
            else:
                item['description'] += '\n' + text.strip()

        item['genres'] = ''
        first = True
        for genre in response.css('div.gameInfo-list:nth-child(4) > div:nth-child(1) > a::text').extract():
            if first:
                item['genres'] += genre.strip()
                first = False
            else:
                item['genres'] += ' ' + genre.strip()

        item['publisher'] = response.css('div.gameInfo-list:nth-child(4) > div:nth-child(2)::text').extract_first().replace(':\n','').strip()

        item['developers'] = response.css('div.gameInfo-list:nth-child(4) > div:nth-child(3)::text').extract_first().replace(':\n','').strip()

        item['platforms'] = ''
        first = True
        for platform in response.css('div.contentPlatformsText a::text').extract():
            if first:
                item['platforms'] += platform.strip()
                first = False
            else:
                item['platforms'] += ' ' + platform.strip()

        item['price'] = response.css('div.gameInfo-list:nth-child(3) > div:nth-child(2)::text').extract_first().replace(':','').strip()

        item['release_date'] = response.css('div.gameInfo-list:nth-child(3) > div:nth-child(1)::text')[1].extract().replace(':','').strip()

        item['ign_score'] = response.css('div.ignRating div.ratingValue::text').extract_first().replace('\n','').strip()

        item['ign_score_phrase'] = response.css('div.ignRating div.ratingText::text').extract_first().replace('\n','').strip()

        item['community_score'] = response.css('div.communityRating div.ratingValue::text').extract_first().replace('\n','').strip()

        item['community_score_phrase'] = response.css('div.communityRating div.ratingText::text').extract_first().replace('\n','').strip()

        item['review_link'] = response.css('div.ignRating a.reviewLink::attr(href)').extract_first().strip()

        yield item
