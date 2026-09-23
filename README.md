# 💱 API de Conversion de Devises

API REST Spring Boot qui convertit une somme d'argent d'une devise à une autre en utilisant des taux de change dynamiques récupérés via l'API externe [ExchangeRate-API](https://open.er-api.com/).

---

## 🚀 Fonctionnalités

- **Conversion de devises** via un endpoint POST (JSON) ou GET (query params)
- **Taux de change en temps réel** via l'API externe open.er-api.com
- **Gestion des erreurs** : devise invalide, montant négatif, problème de connexion
- **Documentation Swagger/OpenAPI** interactive
- **Validation** des entrées avec Bean Validation

## 🛠 Stack Technologique

| Technologie | Usage |
|---|---|
| Spring Boot 4.1 | Framework principal |
| Spring WebClient | Appel HTTP réactif vers l'API externe |
| SpringDoc OpenAPI | Documentation Swagger |
| Lombok | Réduction du code boilerplate |
| Jakarta Validation | Validation des requêtes |
| JUnit 5 + Mockito | Tests unitaires et d'intégration |

---

## 📁 Structure du Projet

```
src/main/java/com/converter/currency_converter/
├── CurrencyConverterApplication.java        # Point d'entrée
├── config/
│   ├── WebClientConfig.java                 # Configuration WebClient
│   └── OpenApiConfig.java                   # Configuration Swagger
├── controller/
│   └── CurrencyConverterController.java     # Endpoints REST
├── dto/
│   ├── ConversionRequest.java               # DTO de requête
│   ├── ConversionResponse.java              # DTO de réponse
│   └── ErrorResponse.java                   # DTO d'erreur
├── exception/
│   └── GlobalExceptionHandler.java          # Gestion centralisée des erreurs
└── service/
    └── CurrencyConverterService.java        # Logique de conversion
```

---

## ⚙️ Installation et Lancement

### Prérequis
- Java 25+
- Maven 3.9+

### Compiler le projet
```bash
./mvnw clean package -DskipTests
```

### Lancer l'application
```bash
# Option normale (si /etc/resolv.conf ne contient pas de search domain)
java -jar target/currency-converter-0.0.1-SNAPSHOT.jar

# Option avec contournement DNS (si erreur de résolution)
java -Dio.netty.resolver.dns.searchDomains= -jar target/currency-converter-0.0.1-SNAPSHOT.jar
```

L'application démarre sur le port **8081**.

---

## 📡 Endpoints

### POST `/api/v1/convert` — Conversion via body JSON

**Requête :**
```bash
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "amount": 100
  }'
```

**Réponse (200 OK) :**
```json
{
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "amount": 100.0,
  "convertedAmount": 85.62,
  "rate": 0.85618
}
```

---

### GET `/api/v1/convert` — Conversion via query params

**Requête :**
```bash
curl "http://localhost:8081/api/v1/convert?from=EUR&to=CDF&amount=50"
```

**Réponse (200 OK) :**
```json
{
  "fromCurrency": "EUR",
  "toCurrency": "CDF",
  "amount": 50.0,
  "convertedAmount": 133693.66,
  "rate": 2673.87
}
```

---

## 🧪 Exemples de Requêtes curl

### ✅ Conversions réussies

```bash
# USD → EUR
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "EUR", "amount": 100}'

# EUR → CDF (Franc congolais)
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "EUR", "toCurrency": "CDF", "amount": 50}'

# GBP → JPY (via GET)
curl "http://localhost:8081/api/v1/convert?from=GBP&to=JPY&amount=200"

# Devise identique (retourne le même montant, taux = 1.0)
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "USD", "amount": 100}'
```

### ❌ Cas d'erreur

```bash
# Montant négatif → 400
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "EUR", "amount": -5}'

# Devise source vide → 400
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "", "toCurrency": "EUR", "amount": 100}'

# Montant manquant → 400
curl -X POST http://localhost:8081/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "EUR"}'

# Paramètre GET manquant → 400
curl "http://localhost:8081/api/v1/convert?from=USD&amount=100"
```

### Format des erreurs

```json
{
  "status": 400,
  "message": "Erreur de validation: {amount=Le montant ne peut pas être vide}",
  "timestamp": "2025-01-15T10:30:00"
}
```

---

## 📖 Swagger UI

L'interface Swagger interactive est disponible à l'adresse :

> **http://localhost:8081/swagger-ui/index.html**

### Utilisation
1. Ouvrez l'URL dans votre navigateur
2. Cliquez sur l'endpoint `POST /api/v1/convert` ou `GET /api/v1/convert`
3. Cliquez sur **"Try it out"**
4. Remplissez les paramètres et cliquez sur **"Execute"**
5. La réponse s'affiche directement dans l'interface

### Documentation OpenAPI (JSON)
> **http://localhost:8081/api-docs**

---

## 🧪 Tests

### Exécuter tous les tests
```bash
./mvnw test
```

### Résultat attendu
```
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Couverture des tests

| Catégorie | Tests | Description |
|---|---|---|
| **Unit tests — Service** | 10 | WebClient mocké, cas nominal, devises invalides, erreur API, arrondi |
| **Integration tests — Controller** | 15 | MockMvc, validation POST/GET, erreurs, params manquants |
| **Context test** | 1 | Démarrage du contexte Spring |

---

## 📝 Données de Test

Quelques paires de devises courantes :

| Devises | Code |
|---|---|
| Dollar US → Euro | USD → EUR |
| Euro → Franc congolais | EUR → CDF |
| Dollar US → Franc congolais | USD → CDF |
| Livre sterling → Yen japonais | GBP → JPY |
| Dollar US → Yen japonais | USD → JPY |
| Euro → Livre sterling | EUR → GBP |

La liste complète des devises supportées est disponible sur [open.er-api.com](https://open.er-api.com/).

---

## 📄 Licence

MIT License
