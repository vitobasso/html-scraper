
# Setup

```bash
brew install leiningen
brew install npm
npm install -g shadow-cljs
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


# Building

#### For Node.js

```bash
npm install # resolves npm dependencies
shadow-cljs compile npm # resolves cljs dependencies and compiles
```