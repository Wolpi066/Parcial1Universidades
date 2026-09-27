# Parcial 1 — Universidades del Mundo

Aplicación Android en Kotlin que busca universidades por país usando la
**Hipolabs Universities API** y muestra la ficha de cada una en un Fragment.

Primer examen de Desarrollo de Aplicaciones Android con Kotlin — **Tema 2**
Emiliano Volpino

## Video demostrativo

**[Ver video](https://drive.google.com/file/d/1QaKZwIN33ZMEej6M7qXLXxdbwMh_5TkG/view?usp=sharing)**

## La API

Se usa la API pública de Hipolabs, que no necesita API key:

```
GET http://universities.hipolabs.com/search?country=Argentina
```

Devuelve **directamente un array** de universidades, sin objeto envoltorio, así
que en Retrofit el tipo de retorno es `Response<List<University>>`.

Cada universidad trae seis campos:

| Campo del JSON | Para qué se usa |
|---|---|
| `name` | el título en la lista y en el detalle |
| `country` | el país |
| `alpha_two_code` | el código ISO de dos letras (AR, ES, US) |
| `state-province` | estado o provincia, cuando la API lo tiene |
| `domains` | los dominios web oficiales |
| `web_pages` | el enlace al sitio, que abre el botón del detalle |

Dos particularidades que condicionaron el código:

- **La API solo responde por HTTP, no por HTTPS.** Desde Android 9 el tráfico sin
  cifrar está bloqueado por defecto, así que hizo falta `usesCleartextTraffic="true"`
  en el manifest. Sin eso, toda llamada falla con *CLEARTEXT not permitted*.
- **El campo `state-province` tiene un guion en el medio**, y un guion no es válido
  en un nombre de Kotlin. Por eso la anotación `@SerializedName("state-province")`
  es obligatoria ahí: sin ella Gson no encuentra el campo y la provincia queda
  siempre nula.

## Pantallas

**1. Búsqueda y listado (`MainActivity`)**
Un campo de texto donde se escribe el país. **No hay botón Buscar**: la lista se
actualiza sola mientras se escribe. Cada fila muestra el código de país, el
nombre de la universidad y su ubicación.

**2. Detalle (`DetailActivity` + `UniversityDetailFragment`)**
Al tocar una universidad se abre la segunda Activity, que hospeda el Fragment con
la ficha: país, código de país, estado/provincia, los dominios oficiales y un
botón con la URL que abre el sitio en el navegador.

## Arquitectura

La app sigue **MVVM (Model - View - ViewModel)** con **Repository Pattern**:

```
app/src/main/java/com/example/parcial1universidades/
├── model/
│   └── University.kt                  Modelo de una universidad (Serializable)
├── service/
│   ├── UniversityApiService.kt        Endpoint de Retrofit
│   └── NetworkChecker.kt              Consulta si hay red disponible
├── repository/
│   └── UniversityRepository.kt        Única puerta de entrada a los datos
├── adapter/
│   └── UniversityAdapter.kt           Adapter del RecyclerView + su ViewHolder
├── viewmodel/
│   ├── UniversityUiState.kt           Los 5 estados posibles de la pantalla
│   ├── UniversityViewModel.kt         Corrutinas, debounce y estados
│   └── UniversityViewModelFactory.kt  Arma el ViewModel con su repositorio
├── MainActivity.kt                    Pantalla 1: buscador y lista
├── DetailActivity.kt                  Pantalla 2: contenedor del Fragment
└── UniversityDetailFragment.kt        Fragment con la ficha
```

El flujo de una búsqueda:

```
MainActivity → UniversityViewModel → UniversityRepository → NetworkChecker + Retrofit
      ↑                                        │
      └──────── UniversityUiState ─────────────┘
```

### Un solo estado en vez de varios LiveData

El ViewModel no expone la lista, el "cargando" y el error por separado: expone
**un único `LiveData<UniversityUiState>`**, donde `UniversityUiState` es una
`sealed class` con los cinco finales posibles.

```kotlin
sealed class UniversityUiState {
    object Idle : UniversityUiState()
    object Loading : UniversityUiState()
    data class Success(val universities: List<University>) : UniversityUiState()
    data class Empty(val country: String) : UniversityUiState()
    data class Error(val message: String) : UniversityUiState()
}
```

La ventaja: los estados no pueden contradecirse. Con tres LiveData sueltos podría
quedar el ProgressBar prendido a la vez que la lista de resultados; acá la
pantalla está en un estado o en otro. Y como es una `sealed class`, el compilador
obliga a cubrir los cinco casos en el `when` de la Activity.

### Búsqueda en tiempo real, con una sola llamada

La consigna pide que la búsqueda sea en tiempo real. Un `TextWatcher` avisa al
ViewModel en cada tecla, pero salir a la API en cada una sería nueve llamadas
para escribir "Argentina".

La solución usa la cancelación de corrutinas:

```kotlin
private var searchJob: Job? = null

fun onQueryChanged(query: String) {
    searchJob?.cancel()
    // ...
    searchJob = viewModelScope.launch(Dispatchers.IO) {
        delay(SEARCH_DELAY_MS)
        _uiState.postValue(UniversityUiState.Loading)
        search(country)
    }
}
```

Cada tecla cancela la corrutina anterior antes de que termine su `delay(400)`.
Solo la última sobrevive y llega a llamar a la API. Verificado: escribir
"Argentina" dispara **una** llamada, no nueve.

Hay un segundo caso donde tampoco conviene llamar: al rotar el celular la
Activity se recrea, el `EditText` restaura su texto y el `TextWatcher` avisa de
nuevo el mismo país. Como el ViewModel sobrevive y ya tiene esos resultados, los
descarta:

```kotlin
if (country == lastSearchedCountry && _uiState.value is UniversityUiState.Success) {
    return
}
```

Solo salta cuando el estado actual ya es `Success` para ese mismo país, así que
reintentar después de un error sigue funcionando.

### Inyección de dependencias

Ni el ViewModel ni el repositorio crean sus propias dependencias: las reciben por
constructor. `MainActivity` arma la cadena y la inyecta con `UniversityViewModelFactory`:

```kotlin
private val viewModel: UniversityViewModel by viewModels {
    UniversityViewModelFactory(
        repository = UniversityRepository(NetworkChecker(applicationContext))
    )
}
```

La fábrica hace falta porque `UniversityViewModel` no tiene constructor vacío, así
que Android no sabe cómo construirlo solo.

### Paso de datos entre pantallas

`University` implementa `Serializable`, así que la universidad seleccionada viaja:

1. de `MainActivity` a `DetailActivity` dentro del **Intent** (`putExtra`), y
2. de `DetailActivity` al `UniversityDetailFragment` dentro del **Bundle de
   argumentos**, con el patrón `newInstance()`.

Los argumentos no se pasan por constructor a propósito: cuando el sistema recrea
un Fragment usa el constructor vacío, así que se perderían. En el Bundle sobreviven.

## Manejo de errores

| Situación | Estado | Qué ve el usuario |
|---|---|---|
| Todavía no escribió nada (o menos de 3 letras) | `Idle` | "Escribí un país para ver sus universidades" |
| Buscando | `Loading` | ProgressBar |
| Hay resultados | `Success` | la lista |
| Ese país no tiene universidades | `Empty` | "No encontramos universidades en ..." |
| Sin red (modo avión, sin datos) | `Error` | aviso al instante, sin esperar timeout |
| Se corta la conexión en el medio | `Error` | `IOException` en el `catch` |
| Error HTTP (404, 500, etc.) | `Error` | el código que devolvió el servidor |

Hay dos controles de red a propósito: `NetworkChecker` evita salir a la API cuando
ya se sabe que no hay conexión, y el `catch (e: IOException)` cubre que la red se
caiga en el medio de la llamada.

## Tecnologías

- Kotlin y **corrutinas** (`suspend`, `viewModelScope`, `Dispatchers.IO`)
- **Retrofit 3.0.0** + **Gson** para consumir la API
- **ViewModel + LiveData** (androidx.lifecycle)
- **RecyclerView** con Adapter y ViewHolder propios
- **View Binding** (sin `findViewById` en ningún lado)
- **Material 3** para los componentes y el tema (con modo oscuro)

## Cómo correr el proyecto

1. Abrir la carpeta con Android Studio y esperar a que termine el Gradle Sync.
2. Elegir un emulador o un dispositivo con Android 7.0 (API 24) o superior.
3. Darle a Run.

Hace falta conexión a internet, tanto para el sync de Gradle como para que la app
pueda consultar la API.

- `minSdk` 24
- `targetSdk` / `compileSdk` 36
