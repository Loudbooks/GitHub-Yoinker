<div align="center">

# GitHub Yoinker
A program to easily download GitHub releases dynamically, written in Kotlin using parallel downloads.
</div>

## Setup

Prerequisites:
- An installation of Java 17+ (Can be validated using `java -version`)

Download:
1. Head over to the [Releases](https://github.com/Loudbooks/Github-Yoinker/releases/latest), and download `yoinker.jar`.
2. Take that jar and put it wherever you like.

Install:
1. Run the jar once to create the default configuration file. It will be created at `./yoinkerconfig.json`.
2. Edit the configuration file to your liking. Make sure to change all the values from the default config to your needs.

## Configuration

`debug` - This value is used to determine whether you would like extra debug output in the console on startup.

`token` - This is the token used for authenticating with GitHub. Head over [here](https://github.com/settings/personal-access-tokens/new) to create a new Fine Grained Token. Give it a name, and select All Repositories if you would like.

`post` - This is the command to be executed after GitHub Yoinker has finished the download process. This can be anything you would like. 

`destinationDirectory` - The directory to which you would like your files to be downloaded to.

`files` - The list of files you would like to download. You can use `${TAGV}` to use the raw release tag including the character v, or `{$TAG}` to remove the character v from the release tag.

Below is an example configuration file.
```json
{
    "debug": true,
    "token": "github_pat_example",
    "post": "java -Xms128M -XX:MaxRAMPercentage=95.0 -Dterminal.jline=false -Dterminal.ansi=true -jar example.jar",
    "destinationDirectory": "plugins",
    "files": [
        {
            "repo": "example/example",
            "filename": "example.jar",
            "tag": "latest"
        },
        {
            "repo": "example2/example2",
            "filename": "example-${TAG}.jar",
            "tag": "latest"
        }
    ]
}
```

## Common Issues

- `Tag was invalid for ...` - Make sure you provided the correct repository name, and a release exists under the tag you provided.
- `File was invalid for ...` - Make sure the file name you provided is valid.