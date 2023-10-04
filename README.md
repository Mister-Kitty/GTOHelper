# GTOHelper
An online poker HUD/tracker (PokerTracker4) -> poker GTO solver (PioSolver) automation tool

The initial purpose of the tool was to alleviate the painful and repetitive process of manually inputting and solving for hands that you wanted to review from your last grind session. While the functionality of scraping data from PokerTracker4's local database, packaging the hands, internally queueing the hands to be solved, and executing the hands in the solver via it's API has been completed, there's still a lot more potential for cool features to be completed that I unfortunately don't have the time/energy to complete. 

Eg.
- Find your average distance from GTO between your play and the solver for each hand in a bulk Position vs. Position computation. This would quantify where you're playing the poorest ~ rather than subjectively guessing where you're underperforming.
- Analyze passivity/aggressiveness imbalances in certain situations by calculating confidence intervals around given solver action frequencies (eg, let's say solver high frequency checks on paired boards when OOP. Well, with some sample size we can deduce if you tend to check sufficiently).
- Filter out hands vs casual players to reduce non-gto hands from sample sizes
- I could go on... Sufficed to say, I'm happy to review and merge in (or give contributor permissions) to anyone who wants to add to this tool. 

![Sessions](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Session%20Tab.png)

![Position v position](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Position%20v%20Position.png)

![Work Queue](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Work%20Queue.png)

"Mr. Kitty#9462" on Discord if needed.
