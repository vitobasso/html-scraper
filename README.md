A HTML scraper with page templates based on css selectors and regex.

## Example

Scraping a search on amazon.co.uk: 

```clojure
(require '[scraper.helper :as s])

(s/scrape-list "amazon"  
  :search-term "cardboard boxes"
  :page-number "1") 
```

A template file `amazon.yml`:
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

## Documentation
- [Maintaining](doc/maintaining.md)
- [Template reference](doc/template-ref.md)


