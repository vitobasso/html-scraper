
# Setup

```bash
brew install leiningen
```


# Running tests

#### Unit tests

```bash
lein test
```

#### Integration tests
Will try to access the internet and scrape an actual website.
```bash
lein test :integration
```
