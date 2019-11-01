
# Setup

```bash
brew install leiningen
```

# Running tests

#### Unit tests

```bash
clj -Atest
```

#### Integration tests
Will try to access the internet and scrape an actual website.
```bash
clj -Ait-test
```