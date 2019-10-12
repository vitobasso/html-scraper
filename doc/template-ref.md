This document describes the keywords used in a template file.

## Common keywords
`select` `extract` `regex` are the basic keywords used in various parts of the template. 

### select
Defines a css selector to find which html elements to scrape from.
The text contained directly within the selected element will be scraped. Nested elements will be ignored.

### extract
Defines which part of the selected html element to scrape from. E.g. an attribute. 

Optional: By default the inner text will be scraped.

---
**Example 1**: Scraping the price and image of an product from being sold in a website.

Input HTML:
```html
<div>
    <img src="https://images.com/image-id">
    <span class="listingPrice">£1,668</span>
</div>
```

Part of the template file:
```yaml
properties:
  - name: 'price'
    select: '.listingPrice'
  - name: 'image'
    select: 'img'
    extract: 'attrs src'
```

Output:
```clojure
{:price "£1,668"
 :image "https://images.com/image-id"}
```

---

### regex
Transforms the scraped text based on the `find` regex.
The output will be according to `replace` with placeholders for regex capturing groups: `${1}`, `${2}`, etc.

Optional.

---
**Example 2**: Scraping latitude and longitude from a page that displays something in a map.

Input HTML:
```html
<script>
    MapLibrary.init({
      coords: {
        lat: '51.5212447680929',
        lon: '-0.057351967042925'
      }
    });
</script>
```

Part of the template file:
```yaml
properties:
  - name: 'latlng'
    select: 'script'
    regex:
      find: '(?s)lat:\s*''([0-9\.\-]+).*lon:\s*''([0-9\.\-]+)'
      replace: '${1},${2}'
```

Output:
```clojure
{:latlng "51.5212447680929,-0.057351967042925"}
```

---
## Page scraping
//TODO

### list-page
- `item-select` 
- `container-select`, `item-split`

### detail-page

## Scraping multiple properties at once
//TODO

### property-tables
`pair-select`, `label`, `value`
