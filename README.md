# Warning

This project is still very much work in progress. That means that with every commit, your project files might no longer
work. So don't blame me for not automatically converting your projects into some newer version of the format, as writing
such thing does not make much sense at this point in time. So just don't expect everything to be perfect, don't create
anything you will regret losing because it took a very long time to make or something like that... Just a warning.

# Logic Sim - A digital logic simulation written in Java

I think the title already tells you a lot about what this project is. But if it does not, here a short explanation: This
program allows you to use nodes and links to build up logical circuits consisting of two base building blocks, the "and"
and "not" operation. These two blocks allow you to build up more complex so-called "blueprints", which in itself can
also contain other blueprints. This system allows you to build gigantic chips, compressed down into a single node.
Another very important part are input and output nodes, which are used to communicate with the outside world so to
speak. So you can build a chip with inputs "A" and "B", and output something on output "Out", and now, when using the
blueprint, you can simply use those input and output pins, just like with a real chip to transfer information to its
inside components.

## Wiki

Please check out the [wiki](./wiki) if you want to learn more about how to use the
program or how everything works behind the scenes.

## Work in progress / TODOs

Following things are still work in progress:

- editing blueprints after compressing them
- zooming in and out in the node editor
- (maybe?) a scripting language for building blueprints
