# Introduction

“WoTNS” is short for “Web of Trust Name Service.” It aims at building a DNS-like service on top of Freenet. It allows short links that look like `http://localhost:8888/tns/WoTNS/LatestPlugin` which will redirect you to a full, more than 100 characters long USK.

## The Problem

Pages in Freenet are referred to by their keys. While keys are unique and perfectly fit for identifying pages they are also more than 100 characters long and thus are completely intypeable. This makes it impossible to spread Freenet keys via offline media such as e.g. business cards, or a URL in a video.

In addition to USKs, SSKs, and CHKs Freenet also offers KSKs which might be used to alleviate that problem. Unfortunately, KSKs are not secure: there is no way for you to make sure that a key you inserted under a specific KSK will not be changed by somebody else.

## The Solution

The Web of Trust plugin offers a solution: each identity is allowed to have properties. By storing short names and long keys in the properties it is possible to have a web-of-trust-based name service in which you can control what your short names point to, and as long as you are deemed trustworthy by the community at large, the target of your short names will never change.

Short names currently have the form: “_identity_ [ ‘@’ _start-of-key_ ] ‘/’ _target_ [ ‘/’ _file-path_ ]”.

_identity_: The identity is the name of the Web of Trust identity, such as “WoTNS” or “Bombe.” This name is case-sensitive, so “wotns” is _not_ the same as “WoTNS!”

_start-of-key_: Sometimes the name of an identity is not enough to distinguish two identities from one another. In this case it is possible to specify the beginning of the identity’s ID to make it possible to pick the correct identity. If two (or more) identities match a given name and no key is specified the one with the highest trust value is chosen!

_target_: The target specifies the name of the target you want to access. It is chosen by the identity and can be a descriptive name such as “homepage” or “flog.”

_file-path_: If you want to link to a specific file within a defined short name, just enter its path after the target. It will then be appended to the defined target.

## Examples

Syntactically valid short names include:

* `WoTNS/LatestVersion`
* `WoTNS/Homepage/activelink.png`
* `WoTNS@DAx/Homepage`

# Usage

## Installation

Installing _WoTNS_ is as easy as entering the plugin’s key into the textbox in the section “Add an Unofficial Plugin” on your Freenet node’s plugin manager page (find it in the “Configuration” menu). Untick the checkbox named “local file only” and press the “Load” button.

Freenet will then download the plugin and start it. As soon as the plugin has been loaded, a new menu entry named “WoTNS” will appear.

## Managing Your Short Names

By clicking the “WoTNS” menu entry you can manage the short names of your identities and which identities are enable for short names at all. If you don’t see any identities listed on this page you have to create one (or more) identities using the “Community” menu, after loading the “Web of Trust” plugin.

An identity must be explicitely enabled to be able to expose short names. Use the “Enable” and “Disable” buttons to switch identities between their enabled and disabled states.

By clicking an enabled identity’s name you can manage the short names for that identity. You can edit and delete existing short names, and you can add new short names.

Note: if you add a new target with the name of an existing target, the existing target will be overwritten.

## Using a Short Name

At the moment using short names is a little bit cumbersome because Freenet itself does not yet have any built-in support for them. So if somebody gives you a short name (e.g. “CoolGuy/Homepage”) all you have to do is to enter the following URL in your browser: `http://localhost:8888/tns/CoolGuy/Homepage`. This example assumes that your node is running on `localhost`, and that CoolGuy’s web of trust identity has already been found by your web of trust plugin.

