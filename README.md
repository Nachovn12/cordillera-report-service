# CREAR ARQUETIPO DESDE INITIALZR

Crear proyecto normal con las dependencias necesarias y correr.

Java extension pack puede generar problemas con Maven desde el terminal por lo que se deben configurar las variables de entorno de sistema
con una ruta directa JAVA HOME y MAVEN HOME

Luego de eso activamos maven en el terminal si no es capaz de reconocer la versión a pesar de haber configurado todo

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"                                                       
$env:Path="C:\Program Files\Java\jdk-21\bin;$env:Path"
echo $env:JAVA_HOME
```

hecho esto ejecutar:

```powershell
    mvn clean install
```

Si todo dió success, correr el proyecto

```powershell
    mvn spring-boot:run
```

Si todo funciona vamos a crear el arquetipo, (asegurar las carpetas con .gitkeep), pero puede que maven de inconvenientes por lo que
configuraremos un perfil de usuario

Abre PowerShell y ejecuta:

```powershell

mkdir $env:USERPROFILE\.m2 -Force
notepad $env:USERPROFILE\.m2\settings.xml

```

Se abrirá el Bloc de notas. Pega esto:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

</settings>
```

Luego vuelve a ejecutar desde la raíz del proyecto:

```console
mvn archetype:create-from-project
```

Cuando termine correctamente, deberías tener esta carpeta en el proyecto:

```console
target\generated-sources\archetype
```

Entra a esa carpeta:

```console
cd target\generated-sources\archetype
```

Y luego instala el arquetipo:

```console
mvn clean install
```

 Si todo funcionó, le debes un café al profe :)

 ahora salimos de la carpeta hasta la raiz del proyecto y intentamos crear el arquetipo, por ejemplo, crear un ms-clientes

```powershell
mvn --% archetype:generate -DarchetypeCatalog=local -DarchetypeGroupId=cl -DarchetypeArtifactId=duoc-archetype -DarchetypeVersion=0.0.1-SNAPSHOT -DgroupId=cl.duoc -DartifactId=ms-clientes -Dversion=1.0.0 -Dpackage=cl.duoc.clientes -DinteractiveMode=false

```

para revisar arquetipos instalados usar

```console
notepad $env:USERPROFILE\.m2\repository\archetype-catalog.xml

```
