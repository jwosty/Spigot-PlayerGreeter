# Spigot-PlayerGreeter
A Minecraft Spigot plugin for creating customizable player greeting messages

## Configuration
- config.yml
  - ``initialGreetingMessage`` - specifies a message to be displayed directly to players when joining the server for the first time
  - ``greetingMessage`` - specifies a message to be displayed directly to returning players when they join the server

The configuration file can be reloaded dynamically using ``/playergreeter:reload``, which has a permission node of ``playergreeter.reload`
