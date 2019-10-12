## Common keywords
The keywords `select` `extract` `regex` are used in various parts of the template. 

### select
Finds html elements based on a css selector.
Scrapes the text contained directly in the html element. Nested elements are ignored.
```yaml
name: 'price'
select: '.listingPrice'
```
In:
```html
<strong class="listingPrice">£1,668 <abbr>pcm</abbr></strong>
```
Out:
```clojure
{:price "£1,668"}
```

### extract
Scrapes from an html attribute rather than the inner text.
```yaml
name: 'image'
select: 'img'
extract: 'attrs src'
```
In:
```html
<img src="https://images.com/image-id">
```
Out:
```clojure
{:image "https://images.com/image-id"}
```

### regex
Transforms the scraped text with a regex. Can be combined with [extract](#extract).

Example: Scraping latitude and longitude from a page that displays something in a map. 
The values could be found in the source code, under an script tag.
```yaml
name: 'latlng'
select: 'script'
regex:
  find: '(?s)lat:\s*''([0-9\.\-]+).*lon:\s*''([0-9\.\-]+)'
  replace: '${1},${2}'
```
In:
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
Out:
```clojure
{:latlng "51.5212447680929,-0.057351967042925"}
```


## Page scraping
//TODO

### list-page
#### item-select
#### container-select, item-split
### properties
    
### detail-page


## Scraping multiple properties at once
//TODO

### property-tables
#### pair-select
#### label
#### value
