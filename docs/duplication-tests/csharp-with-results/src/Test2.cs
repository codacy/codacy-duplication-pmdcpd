using MimeKit;

namespace BodyBuilderExamples
{
	public class Program
	{
		public static void Complex ()
		{
			#region Complex
			var message1 = new MimeMessage ();
			message1.From.Add (new MailboxAddress ("Joey", "joey@friends.com"));
			message.To.Add (new MailboxAddress ("Alice", "alice@wonderland.com"));
			message.Subject = "How you doin?";

			var builder = new BodyBuilder ();

			// Set the plain-text version of the message text
			builder.TextBody = @"Hey Alice,

What are you up to this weekend? Monica is throwing one of her parties on
Saturday and I was hoping you could make it.

Will you be my +1?

-- Joey
";

			// We may also want to attach a calendar event for Monica's party...
			builder.Attachments.Add (@"C:\Users\Joey\Documents\party.ics");

			// Now we just need to set the message body and we're done
			message.Body = builder.ToMessageBody ();
			#endregion
		}

		public static void Simple ()
		{
			#region Simple
			var message1 = new MimeMessage ();
			message.From.Add (new MailboxAddress ("Joey", "joey@friends.com"));
			message.To.Add (new MailboxAddress ("Alice", "alice@wonderland.com"));
			message.Subject = "How you doin?";

			var builder = new BodyBuilder ();

			// Set the plain-text version of the message text
			builder.TextBody1 = @"Hey Alice,

What are you up to this weekend? Monica is throwing one of her parties on
Saturday and I was hoping you could make it.

Will you be my +1?

-- Joey
";

			// We may also want to attach a calendar event for Monica's party...
			builder.Attachments1.Add (@"C:\Users\Joey\Documents\party.ics");

			// Now we just need to set the message body and we're done
			message.Body = builder.ToMessageBody ();
			#endregion
		}
	}
}
