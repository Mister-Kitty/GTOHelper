# GTOHelper
An online poker HUD (PokerTracker4) -> poker GTO solver (PioSolver) automation tool

This tool has a few purposes, the core of which is to alleviate the painful and repetitive process of manually inputting and solving for hands that you wanted to review from your grind sessions!

While the functionality of scraping data from PokerTracker4's local database, executing the hands in the solver via it's API, and many of the steps in between have been completed, there's still a lot more cool features I aim to do.

Eg.
- After solving a session, sort and display the hands by how far your play differs from optimal ~ quantifying where you've made mistakes.
- One click button to view the solve & your own replay of a hand, to facilitate analysis.
- Do confidence interval calculations, and apply these to bulk board and position vs. position computations. For instance, you can queue paired-board OOP or IP flops, and bulk analyze how you *actually* play them ~ rather than subjectively guessing where you're underperforming and missing action frequencies. Don't subjectivly guess where you need to improve ~ we can objectivly tell you!
- Analyze opponent frequencies, even if their cards aren't known. If they should be check-raising at certain frequencies in certain situations, we don't need to know their cards to deduce where they're making mistakes.
- Filter out hands vs casual players to reduce non-gto hands from sample sizes
- Other stuff that I can't think of right now, or that users will request for me to make.

![Sessions](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Session%20Tab.png)

![Position v position](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Position%20v%20Position.png)

![Work Queue](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Work%20Queue.png)

Right now, only a default 100BB 6max range is available. This means this tool isn't currently too useful for tournaments. However, the very next thing I'll be working on are the changes for both the GUI and backend that are required to match hand ranges to BB ranges. So it shouldn't take _too_ long to see this feature completed.

# How to get it running
1. On the top right hand of this github page, under the About section, you should see a Releases section. Click 0.1.0.0, as shown here:
 
![Releases](https://raw.githubusercontent.com/Mister-Kitty/Mister-Kitty.github.io/main/img/GTOHelper/releases.png)
   
2. Then click and download GTOHelper.exe. Place in its own folder, wherever you want. GTOHelper does not install. 
3. When you execute the application, it will decompress default files. So do be sure to actually put it in an folder.
   
![Folder](https://raw.githubusercontent.com/Mister-Kitty/Mister-Kitty.github.io/main/img/GTOHelper/expanded_folder.png) 

Note: You'll almost certainly get a Windows SmartScreen warning when you execute the exe. On the warning, click 'more info' and 'run anyway'.
These warnings are avoidable by purchasing a Code Signing Certificate from Digisign or wherever ~ but they're very expensive and don't validate the code in any way. It's a borderline scam, in my opinion.

"Mr. Kitty#9462" on Discord if you need assistance. I'm also in the Piosolver Discord, in case you can't message me without a shared server in common.
