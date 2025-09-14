Release instructions for Wallet [MEO] and Permissions [MEO]

1) Artifacts
- Wallet jar: Wallet [MEO]/build/libs/wallet-1.0.0.jar
- Permissions jar: Permissions [MEO]/build/libs/Permissions-MEO-1.0.0-1.12.2.jar

2) Quick install (server)
- Stop your server.
- Copy the two JARs into the server's mods/ folder.
- If you run a Forge 1.12.2 server, ensure the server uses Java 8.
- Start the server. Both mods register commands automatically.

3) Commands (Wallet)
- /wallet <subcommand>
- /balance, /bal, /money — view balance
- /pay <player> <amount> — pay another player
- /baltop — top balances
- /wallet gui — open GUI (client-side)
- /wallet reload — reload config (op only)
- /wallet save — force save (op only)

4) Commands (Permissions)
- /permissions <subcommand> (see in-game usage for full list)

5) Known developer notes
- These mods target Minecraft 1.12.2 and Forge 14.23.5.x. Use Java 8 for dev runs and server runtime.
- If you encounter startup messages about unreadable Gradle cache jars (asm-6.2 etc.), they are dependency cache issues in the development environment and not mod code problems. Do not clear caches on production servers.

6) Licensing and distribution
- Confirm you have the right to publish. Update README files with license text if necessary before public release.

7) Testing checklist before public release
- Verify server startup with both JARs present and no fatal errors in logs.
- Verify player can run /balance, /pay, /baltop on server.
- Verify GUI opens client-side when right command issued and client has corresponding mod JAR installed.
- Test autosave and manual /wallet save.

8) Build reproducibility
- To rebuild jars: run gradlew clean build -x test inside each module folder.
- Use Java 8 for building and running the dev client/server.


