Estructura de Base de Datos - Cloud Firestore

Este diagrama ilustra la estructura de la colección principal reportes. El diseño utiliza un esquema flexible donde el campo detalles almacena información diferente según la categoría seleccionada.

Diagrama Visual de la Estructura

classDiagram
    class Coleccion_Reportes {
        +String id
        +String categoria
        +String descripcion
        +Double latitud
        +Double longitud
        +String urlImagen
        +Timestamp fechaRegistro
        +String alias
        +Map detalles
    }

    class Detalles_Servicios {
        +String tipo_servicio
    }

    class Detalles_Robo {
        +String objetos_sustraidos
    }

    class Detalles_Genero {
        +String tipo_violencia
        +String relacion_agresor
    }

    class Detalles_Corrupcion {
        +String dependencia
        +String servidor_publico
    }
    
    class Detalles_Narcomenudeo {
        +String actividad_sospechosa
        +String descripcion_vehiculos
    }

    Coleccion_Reportes -- Detalles_Servicios : contiene (si es Servicios Públicos)
    Coleccion_Reportes -- Detalles_Robo : contiene (si es Robo)
    Coleccion_Reportes -- Detalles_Genero : contiene (si es Violencia de Género)
    Coleccion_Reportes -- Detalles_Corrupcion : contiene (si es Corrupción)
    Coleccion_Reportes -- Detalles_Narcomenudeo : contiene (si es Narcomenudeo)


Ejemplo de Documento JSON

A continuación se muestra un ejemplo real de cómo se ve un documento almacenado en Firestore para la categoría "Violencia de Género":

{
  "id": "a1b2c3d4-e5f6...",
  "categoria": "Violencia de Género",
  "descripcion": "Agresión verbal en vía pública cerca del parque.",
  "latitud": 19.432608,
  "longitud": -99.133209,
  "urlImagen": "[https://firebasestorage.googleapis.com/](https://firebasestorage.googleapis.com/)...",
  "fechaRegistro": "Timestamp(seconds=1767890000, nanoseconds=0)",
  "alias": "Anónimo",
  "detalles": {
      "tipo_violencia": "Verbal/Psicológica",
      "relacion_agresor": "Desconocido"
  }
}
Diccionario de Campos

id: Identificador único generado (UUID) para el documento.

categoria: Define el tipo de reporte (ej. "Robo o Asalto", "Servicios Públicos").

descripcion: Narrativa general de los hechos escrita por el usuario.

latitud / longitud: Coordenadas geográficas exactas tomadas del GPS.

urlImagen: Enlace directo a la fotografía de evidencia almacenada en Cloud Storage.

fechaRegistro: Fecha y hora exacta de la creación del reporte (Server Timestamp).

alias: (Opcional) Nombre o apodo del ciudadano.

detalles: Objeto dinámico (Mapa) que contiene las respuestas del formulario diferenciado.

|Mapa de Calor|Reportes|Modulo de Ayuda|
|:---:|:---:|:---:|
|![](https://github.com/user-attachments/assets/f117fc10-9993-4d78-9734-92efdd320e7a)|![](https://github.com/user-attachments/assets/efffefba-1b28-41bb-a4c6-78605ea8539f)|![](https://github.com/user-attachments/assets/4be6c2d1-b6e8-4354-96f3-81ac749fa96f)|
|Reporte de Robo|Filtro de Emergencias|Filtro de Violencia de Genero|
|:---:|:---:|:---:|
|![](https://github.com/user-attachments/assets/bbc68c46-10fe-49eb-b98d-8387c5e30980)|![](https://github.com/user-attachments/assets/1426190e-a344-498f-a5ae-0ca089329a10)|![](https://github.com/user-attachments/assets/26e04bee-1031-4e83-9f3a-c7da37dc6d59)|

