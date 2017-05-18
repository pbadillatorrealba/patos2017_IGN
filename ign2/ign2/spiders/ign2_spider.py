import scrapy

class IGNItem(scrapy.Item):
    title = scrapy.Field()
    short_description = scrapy.Field()
    genre = scrapy.Field()
    platform = scrapy.Field()
    release_date = scrapy.Field()
    price = scrapy.Field()

    review_link = scrapy.Field()
    ign_score = scrapy.Field()
    community_score= scrapy.Field()
    ign_score_phrase = scrapy.Field()
    community_score_phrase = scrapy.Field()

    publisher = scrapy.Field()
    developer = scrapy.Field()

    editors_choice = scrapy.Field()
    url = scrapy.Field()
    description = scrapy.Field()


class IGNSpider(scrapy.Spider):
    name = "ign2_spider"
    start_urls = [
        'http://www.ign.com/games/civilization-beyond-earth/pc-20016115'
    ]

    def parse(self, response):

        item = IGNItem()

        content_title = response.css('div.shell.container-content-main div.contentHeaderNav.noprint div.contentHead.clear h1.contentTitle')
        item['title'] = response.css('.contentTitle > a:nth-child(1)::text').extract_first()

        content_platform = response.css('div.shell.container-content-main div.contentHeaderNav.noprint div.contentHead.clear div.contentDetails.clear div.contentPlatforms.single div.contentPlatformsText')
        item['platform'] = ''
        i = 1
        while(True):
            tmp = content_platform.css('.contentPlatformsText > span:nth-child('+str(i)+') > a:nth-child(1)::text').extract_first()
            if (tmp == None):
                break
            item['platform'] += tmp + " "
            i += 1

        game_info_left = response.css('div.shell.container-content-main div.container_24.clear.releaseObject div.contentBackground.grid_16.hreview div.mainContent div.contentModule div.aboutGame.clear div div#summary.ign-tabContent div.gameInfo div.gameInfo-list.leftColumn')
        item['release_date'] = game_info_left.css('div.gameInfo-list:nth-child(3) > div:nth-child(1)::text')[1].extract()
        item['price'] = game_info_left.css('div.gameInfo-list:nth-child(3) > div:nth-child(2)::text').extract_first()

        game_info = response.css('div.shell.container-content-main div.container_24.clear.releaseObject div.contentBackground.grid_16.hreview div.mainContent div.contentModule div.aboutGame.clear div div#summary.ign-tabContent div.gameInfo div.gameInfo-list')
        '''------------------------------------------------------'''
        # Aqui aun hay problemas!
        item['genre'] = ''
        genres = game_info.css('div.gameInfo-list:nth-child(4) > div:nth-child(1) > *::text').extract()
        for genre in genres:
            item['genre'] += '' if genre == 'Genre \n' else genre
        '''
        while(True):
            tmp = game_info.css('div.gameInfo-list:nth-child(4) > div:nth-child(1) > *:nth-child('+str(i)+')::text').extract_first()
            if (tmp == None):
                break
            item['genre'] += tmp + " "
            i+=1
            ----------------------------------------------------------
        '''
        item['publisher'] = game_info.css('div.gameInfo-list:nth-child(4) > div:nth-child(2)::text').extract_first()
        item['developer'] = game_info.css('div.gameInfo-list:nth-child(4) > div:nth-child(3)::text').extract_first()

        ign_eval = response.css('div.shell.container-content-main div.container_24.clear.releaseObject div.grid_8 div.rightrail-module div.ratingWidget div.ratingRows div.ignRating.ratingRow')
        item['ign_score'] = ign_eval.css('div.ratingValue:nth-child(4)::text').extract_first()
        item['ign_score_phrase'] = ign_eval.css('div.ratingText:nth-child(5)::text').extract_first()
        item['review_link'] = ign_eval.xpath("//a[@class='reviewLink']/@href").extract_first()

        community_eval = response.css('div.shell.container-content-main div.container_24.clear.releaseObject div.grid_8 div.rightrail-module div.ratingWidget div.ratingRows div.communityRating.ratingRow')
        item['community_score'] = community_eval.css('.communityRating > div:nth-child(2)::text').extract_first()
        item['community_score_phrase'] = community_eval.css('.communityRating > div:nth-child(3)::text').extract_first()

        description = response.css('div.shell.container-content-main div.container_24.clear.releaseObject div.contentBackground.grid_16.hreview div.mainContent div.contentModule div.aboutGame.clear div div#summary.ign-tabContent div.gameInfo')
        item['description'] = ''
        i = 1
        while (True):
            tmp = description.css('.gameInfo > p:nth-child('+str(i)+')::text').extract_first()
            if (tmp == None):
                break
            item['description'] += tmp
            i+=1

        #Strip Zone - Limpia los string, pero aun no funciona con publisher y developer.
        item['title'] = item['title'].strip()
        #   item['release_date'] = item['release_date'].strip()
        item['genre'] = item['genre'].strip()
        item['publisher'] = item['publisher'].strip(":").strip()
        item['developer'] = item['developer'].strip(":").strip()
        #item['genre']
        item['ign_score'] = item['ign_score'].strip()
        item['ign_score_phrase'] = item['ign_score_phrase'].strip()
        item['community_score'] = item['community_score'].strip()
        item['community_score_phrase'] = item['community_score_phrase'].strip()
        item['description'] = item['description'].strip()
        item['release_date'] = item['release_date'].strip()
        item['genre'] = item['genre'].replace('Genre','').strip()

        print item
