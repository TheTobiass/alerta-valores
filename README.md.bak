# Alerta Valores - Detector de Golpes do Banco Central

Este projeto implementa uma API para detectar tentativas de golpe relacionadas ao sistema "Valores a Receber" do Banco Central do Brasil.

## Sobre o Projeto

O sistema analisa mensagens e URLs suspeitas para identificar possíveis golpes relacionados ao serviço "Valores a Receber" do Banco Central, verificando:

1. URLs suspeitas ou diferentes do site oficial (https://valoresareceber.bcb.gov.br)
2. Solicitações de pagamento (o serviço é totalmente gratuito)
3. Solicitações de dados pessoais (o BC nunca solicita por mensagem)
4. Menções a cartão de crédito (não existe recall ou devolução de cartão)
5. Promessas de valores específicos (o BC nunca informa valores por mensagem)

## Como Executar

### Opção 1: Execução Local

#### Requisitos
- Java 21
- Maven (ou use o Maven Wrapper incluído)

#### 1. Gerar o JAR
```powershell
.\mvnw.cmd clean package
```

#### 2. Configurar Google Safe Browsing API (recomendado)
```powershell
$env:GOOGLE_API_KEY = 'SUA_CHAVE_AQUI'
```

#### 3. Iniciar a aplicação
```powershell
java -jar target\alerta-valores-0.0.1-SNAPSHOT.jar
```

### Opção 2: Execução com Docker

#### Requisitos
- Docker
- Docker Compose

#### 1. Construir e iniciar a aplicação
```sh
docker compose up --build
```

#### Detalhes do Serviço Docker
- **Service Name:** `java-app`
- **Porta:** `8080` (mapeada para porta 8080 do host)
- **Network:** `app-network`
- **Segurança:** Executa como usuário não-root (`appuser`)
- **JVM:** Configurado com `-XX:MaxRAMPercentage=80.0`

## Como Testar

Você pode usar qualquer cliente HTTP (cURL, Postman, REST Client) para testar a API. Exemplos usando PowerShell:

### 1. Teste com Mensagem Suspeita (Golpe)

```powershell
$body = @{
    mensagem = "URGENTE: Você tem R$ 5.432,10 em valores a receber do Banco Central. Faça um PIX de R$ 10 para liberar."
    canal = "whatsapp"
} | ConvertTo-Json

curl.exe -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/url/verificar
```

### 2. Teste com Mensagem Benigna

```powershell
$body = @{
    mensagem = "Para consultar valores a receber, acesse o site oficial do Banco Central"
    url = "https://valoresareceber.bcb.gov.br"
    canal = "email"
} | ConvertTo-Json

curl.exe -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/url/verificar
```

### 3. Teste com URL Maliciosa

```powershell
$body = @{
    mensagem = "Confira seus valores a receber do Banco Central"
    url = "http://banco-central-valores.com.br"
    canal = "sms"
} | ConvertTo-Json

curl.exe -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/url/verificar
```

## Interpretando os Resultados

A API retorna um JSON com dois campos:

```json
{
    "seguro": false,
    "detalhe": "⚠️ Detalhes sobre por que a mensagem é considerada suspeita..."
}
```

### Tipos de Alertas:

1. **URL Não Oficial**: Qualquer URL diferente de https://valoresareceber.bcb.gov.br
2. **Solicitação de Pagamento**: Menções a PIX, taxa, pagamento
3. **Dados Pessoais**: Solicitações de CPF, senha ou dados pessoais
4. **Cartão de Crédito**: Menções a recall ou devolução de cartão
5. **Valores Específicos**: Promessas de valores monetários específicos

## Monitoramento

Para monitorar o comportamento da aplicação, observe os logs no terminal. Os logs incluem:

- Canal de origem da mensagem (WhatsApp, SMS, email)
- Resultado da análise (Seguro/Suspeito)
- Detalhes de URLs verificadas via Google Safe Browsing API