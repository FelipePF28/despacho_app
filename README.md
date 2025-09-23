# DespachoApp (Android + Java)

Aplicación móvil para una distribuidora de alimentos que permite **autenticación con Firebase (email/contraseña)**, **cálculo de costo de despacho**, **registro de ubicación en tiempo real en Firebase Realtime Database** y **alerta por cadena de frío** cuando la temperatura del camión supera el umbral.

## Tecnologías
- Android (Java) — minSdk 21 (Lollipop), targetSdk 34
- Firebase Authentication, Realtime Database
- Google Play Services Location

## Reglas de negocio (costo de despacho)
- Total ≥ $50.000 y distancia ≤ 20 km ⇒ **$0**
- $25.000–$49.999 ⇒ **$150/km**
- < $25.000 ⇒ **$300/km**

## Arquitectura y rutas de datos
# despacho_app
app despacho, semana 6
/users/{uid}/locations/{epoch} -> { lat, lng, provider }
/orders/{orderId} -> { userId, total, distanceKm, shippingCost }
/fleet/{truckId}/freezerTemp -> number (°C)
## Reglas de seguridad (demo)
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "users": { "$uid": { ".read": "auth.uid === $uid", ".write": "auth.uid === $uid" } }
  }
}
