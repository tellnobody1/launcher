# launcher

It is a fast, free, minimalist Android Launcher. Even though this fork is
heavily modified this launcher is a tribute to the ideas and concepts of HayaiLauncher.

## Screenshots

<table style="width:100%">
<tr>
<th>
<a href="https://user-images.githubusercontent.com/396546/27193525-faef906e-51b3-11e7-8a44-56f66307156e.png">
<img alt="Nexus 6P Portrait" width="30%" 
    src="https://user-images.githubusercontent.com/396546/27193525-faef906e-51b3-11e7-8a44-56f66307156e.png">
</a>
</th>
<th>

<a href="https://user-images.githubusercontent.com/396546/27193524-faec6678-51b3-11e7-9510-b84700823e42.png">
<img alt="Nexus 6P Landscape" width="40%"
    src="https://user-images.githubusercontent.com/396546/27193524-faec6678-51b3-11e7-9510-b84700823e42.png">
</a></th></tr>
<td align="center">
    Nexus 6P (Landscape)
</td><td align="center">
    Nexus 6P (Portrait)
</td>


</table>

## Development 

### Releasing a new version

1. Cut a new branch named `release/<version>`, where version should be the intended release version followng semantic
   versioning scheme, f.ex. `release/1.2.3`
2. In the release branch:
   * update the release version in the _app/build.gradle_ (do not forget to omit the `-SNAPSHOT` suffix)
     ```
     version "1.2.2"  // <- here
     android {
         defaultConfig {
             ...
             versionName "1.2.2" // <- and here
         }
         ...
     }
     ```
   * update `CHANGELOG.md`. Follow [keepachangelog](https://keepachangelog.com/en/1.0.0/) guidelines
   * create a changelog file under `fastlane/metadata/android/en-US/changelogs/` for the f-droid store
   * commit and push
3. Open a pull-request
4. Once the PR is merged, create an annotated tag on the merge commit with the version of the release
5. Finally, commit to _main_ (or open a PR) bumping a bugfix part of the version and adding `-SNAPSHOT` suffix to it

**NOTE:** the git tag must literally match the version specified in the `app/build.gradle`. This is required for the
_F-Droid_'s fastlane pipeline to pick up new versions of the application correctly.
