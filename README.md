GeekyRSS
========

Android application for following RSS feeds


When the application starts, the user is able to see a list of all the feeds that they have subscribed to along with the number of read and unread news for each feed. 
Syncing of the news is done automatically when the initial screen is loaded if the application finds that there is network (Wi-Fi or Mobile data) connection. 
All the feeds (title, text, URL to the real news, picture and any additional data) are stored in a database for offline reviewing. 
The user is also be able to subscribe to a new feed by providing its URL as well as unsubscribe from existing feeds.

When the user clicks on a feed from the initial screen, all the news items that it contains are presented on a new screen in a way that the new (unread) ones can be distinguished from the old - readed ones are Strikethrough styled.
A news item is considered old if it is opened.
The user is also  able to look at a list of featured news from all the feeds that they have subscribed to. Featured news is one that contains a key word or a sequence of words in its title or summarized text. 
The user is able to manipulate, i.e. add or delete, the list of key words. This is done from a dedicated screen.

There is a background job that regularly checks if the database contains too many entries and that cleans up the oldest ones (the older than 10 days).
