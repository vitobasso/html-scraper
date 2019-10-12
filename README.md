A HTML scraper with page templates powered by css selectors and regex.

# Example

Scraping a search on amazon.co.uk using the following code snippet and template definition: 

```clojure
(require '[scraper.scraping :as scr])
(require '[scraper.config :as cfg])
(def template (cfg/load-config "amazon"))

;                search term          page
(scr/scrape-list "cardboard boxes"    1       template) 
```

The template file `amazon.yml`:
```yaml
# a "list page" will have a bunch of items that follow the same structure
list-page:

  # the exact url path will vary with pagination and query params
  url: 'https://www.amazon.co.uk/s?k=${search-term}&page=${page-number}'
  
  # css selector to find every item in the page 
  item-select: '.s-result-list > div'
  
  # for each property we define a css selector to find the data within the item html 
  # that was already cut out from the page 
  properties:
    - name: 'name'
      select: 'span.a-text-normal'
    - name: 'image'
      select: '.s-image'
      extract: 'attrs src'
    - name: 'price'
      select: '.a-price > .a-offscreen'
```

Ouptut:
```
({:name "SmoothMove Heavy Duty Double Wall Cardboard Moving and Storage Boxes with Handles - 39 Litre, 26 x 32 x 47 cm (10 Pack)", :image "https://m.media-amazon.com/images/I/812ztwbCWfL._AC_UY218_ML3_.jpg", :price "£15.99"} 
 {:name "10 Strong Extra Large Cardboard Storage Packing Moving House Boxes Double Walled with Carry Handles and Room List Fragile Tape Marker Pen and 10 Large Fragile Stickers 53cm x 53cm x 41cm 115 Litres", :image "https://m.media-amazon.com/images/I/71q2DEaaEaL._AC_UY218_ML3_.jpg", :price "£24.99"} 
 {:name "Ambassador Packing Carton Double Wall Strong Flat-packed, 457x305x305mm, Pack of 15 (307688)", :image "https://m.media-amazon.com/images/I/31NuAUDeGKL._AC_UY218_ML3_.jpg", :price "£16.45"}
 ... 
```

# Template file reference


## Basic scraping

#### select
Finds html elements based on a css selector.
Scrapes the text contained directly in the html element. Nested elements are ignored.
```yaml
name: 'price'
select: '.listingPrice'
```
##### In
```html
<strong class="listingPrice">£1,668 <abbr>pcm</abbr></strong>
```
##### Out
```clojure
{:price "£1,668"}
```

#### extract
Scrapes from an html attribute rather than the inner text.
```yaml
name: 'image'
select: 'img'
extract: 'attrs src'
```
##### In
```html
<img src="https://images.com/image-id">
```
##### Out
```clojure
{:image "https://images.com/image-id"}
```

#### regex
```html
<script>
    SR.listing.detail.init({
      coords: {
        lat: '51.5212447680929',
        lon: '-0.057351967042925'
      }
    });
</script>
```
##### Template
```yaml
name: 'latlng'
select: 'script'
regex:
  find: '(?s)lat:\s*''([0-9\.\-]+).*lon:\s*''([0-9\.\-]+)'
  replace: '${1},${2}'
```
##### Output
```clojure
{:latlng "51.5212447680929,-0.057351967042925"}
```



#### list-page
##### item-select
##### container-select, item-split
    
#### detail-page

#### properties

#### property-tables
##### pair-select
##### label
##### value
