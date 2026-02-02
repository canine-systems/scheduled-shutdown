# Scheduled Shutdown

![Modrinth Version](https://img.shields.io/modrinth/v/KaJrPoLs) ![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/canine-systems/scheduled-shutdown/main.yml)

Make sure players have a warning before an impending server shutdown!

This mod adds a `/shutdown` and `/update` command, with 3 subcommands. If no subcommand is specified, it is equivalent to `/shutdown slow` or `/update slow`.

These are the subcommands:

- `slow`: Schedule a shutdown with a 10-minute countdown. Creates empty `minecraft.disable` file.
- `quick`: Schedule a shutdown with a 100-second countdown. Creates empty `minecraft.disable` file.
- `cancel`: Cancel a scheduled shutdown. Deletes `minecraft.disable` file if it exists.

The `/update` variants will additionally create and delete an empty `minecraft.needs-update` file.

The `minecraft.disable` and `minecraft.needs-update` files can be useful for external tools that help manage your Minecraft server.

If all you want is to have a nice countdown, you can just ignore them. :)

I personally use them like this:
- the existence of `minecraft.disable` will [pause auto-restart functionality](https://github.com/canine-systems/advanced-colonies-server/blob/059d3360b9b85ac2c6ba49d95a4f2b7c98d7e0cc/packaging/systemd/minecraft.service#L4).
- the existence of `minecraft.needs-update` will trigger an update of the server modpack.
- my modpack includes Automodpack, so clients get updates when they next reconnect after an update.

![A screenshot showing the boss bar and message immediately after running the `/shutdown quick` command.](screenshots/shutdown-immediate.png)

![A screenshot showing the boss bar and message, with 30 seconds remaining from a `/shutdown quick` command.](screenshots/shutdown-30s-remaining.png)
