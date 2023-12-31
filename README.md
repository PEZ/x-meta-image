# Playground: X/Twitter Link Metadata Image Overlay

Adding back article info to links shared on [X](https://x.com) (formerly [Twitter](https://twitter.com))

![A defiant and angry kitten breaking through a scattering door decorated with a huge X sign. Adding Back Article Information to Links on X. Peter Strömberg a.k.a. PEZ. Placing article header, author, and description as an image overlay on X Link Share Images.](assets/kitten-twitter.jpg)

A REPL playground using [Clojure](https://clojure.org), [Membrane](https://github.com/phronmophobic/membrane), and [babashka.fs](https://github.com/babashka/fs).

## The Problem

X recently stopped displaying article information, such as header and brief summary for links posted on the platform. This gets weird for several reasons. Generally it hides what the linked article is about. See [this image](assets/twitter-summary-card.jpg) for how it used to look.

## The Solution

For the blog [A few words from Agical](https://blog.agical.se/en) I made it so that the Twitter Card Image has an overlay with the missing article information. When sharing articles from the blog on X, the image preview look like the one decorating this README.

As it was very fun creating the solutions in the REPL, I decided to create this little playground project and share it with you. I've tried to make it really easy to use.

## Usage

If you have a Clojure development environment set up, open this project, start its REPL and connect to it with your editor of choice. Then open the file [src/pez/x_meta/compose.clj](src/pez/x_meta/compose.clj), and follow the instructions there.

Regardless if you're now or old to Clojure, you can watch this video and get the picture (pun intended) of how to use this project.

[![That defiant and angry kitten breaking through a scattering door decorated with a huge X sign again. This time with a Clojure logo and a Youtube playbutton added. Texts: Interactive Programming. Coding fun Powered by Clojure & Membrane](assets/kitten-youtube-github.jpg)](https://www.youtube.com/watch?v=ImBji-1bKkc)

(See [src/pez/x_meta/compose_yt_gh.clj](src/pez/x_meta/compose_yt_gh.clj) for a version of the app tha created the thumbnail used on Youtube, as well as ^that^ thumbnail.)

If you are new to Clojure, these are the things you need:

1. Java (I can recommend [sdk-man](https://sdkman.io/) for installing Java)
1. The [Clojure CLI](https://clojure.org/guides/install_clojure)
1. An editor with Clojure support (I use VS Code with [Calva](https://calva.io))
1. Some basic Clojure knowledge. Specifically:
   1. How to start a REPL and connect it to your editor
   1. How to evaluate/load Clojure files in the REPL, using your editor
   1. How to evaluate Clojure forms (expressions/pieces of Clojure code) in your editor

A way to gain that basic Clojure knowledge is watch that video. And to install [Calva](https://calva.io) in VS Code, and then issue the command **Calva: Fire up the Getting Start REPL**. (Once you have Java and Clojure installed.)

If VS Code is not your cup of tea. Check my [Getting Started with Clojure Guide](https://calva.io/get-started-with-clojure/) out anyway. It has some links to other Clojure learning resources.

## Why Membrane?

[Membrane](https://github.com/phronmophobic/membrane) is _A Simple UI Library That Runs Anywhere_, created by Adrian Smith, [@phronmophobic](phronmophobic). It may seem as a bit of overkill to use it for just placing some text on an image? I'd say it isn't. Even if I don't need a lot of the UI framework things in Membrane, it's very lightweight and performant and has the right API and philosophy for making this kind of task easy and fun to solve. Laying out text is not simple to solve without a good library. The text layed out here has these requirements:

* The title, author, and description should be layed out vertically, with some gap between them
* We want to style the texts some, bold for the title, italics for the author, and we want to choose fonts freely.
* The title and the description can be longer than what fits the width of the image, and then the text should break and continue below.
* When text breaks up in several lines, the whole text block gets higher
* The overlay is placed with a fixed margin to the bottom

The last two items can get a tricky, but with Membrane it is both simple and easy. Membrane also solves the other requirements beautifully. In addition to this, the layout and composition of views in a UI fits well for the layout and composition we need for the image. With Membrane, the image and texts gets to be UI elements and very straightforward to compose.

Perhaps the most important part is the interactivity it provides. When running the composition in the Membrane UI, it gets fully reactive to changes in all code that build it up. And you can inspect everything about the app (composition) as it is running. It may be among the most interactive programming I have ever experienced, tbh.

## Happy coding! ♥️

I hope you will try this and enjoy. Please don't hesitate to use the issues in this repository to ask for help, any questions, or suggest improvement.

## License

I, Peter Strömberg aka [PEZ](https://github.com/PEZ), hereby declare the code in this repository as being free as in beer, and free as in liberty. Use the code as you wish, at your own risk.