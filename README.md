# httpserver
Http Json Server - Jaxrs

Servidor http-Json.

Utiliza la clase HttpServer de Java para levantar un web server en un puerto determinado, y con una cantidad de hilos para manejar la concurrencia.
Todas las clases que se comporten como contextos web heredan de WebContext, y tienen que tener las anotaciones de jax-rs
para mapear los metodos get,put,post y delete.
Actualmente solo maneja tipos de datos simples de java, pero esta abierto para manejar entidades de negocio particulares para cada caso

Utiliza java 7 por lo que no es posible obtener los nombres de los parametros en tiempo de ejecución, pero si se utilza java 8, el jdk ya contiene metodos en la clase Class para obtener los nombres de los parametros de los métodos.
