package eu.jsparrow.ui;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

/**
 * Similar to the {@link SimpleContentProposalProvider}, this class uses a
 * static list of Strings to propose content.
 * <p/>
 * The purpose and main difference is that the comparison checks for any match,
 * regardless of whether or not the start matches.
 * 
 * @since 3.20.0
 */
public class PartialMatchContentProposalProvider implements IContentProposalProvider {

	private String[] proposals;

	public PartialMatchContentProposalProvider(String... proposals) {
		super();
		this.proposals = proposals;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		ArrayList<ContentProposal> list = new ArrayList<>();
		for (String proposal : proposals) {
			if (proposal.length() >= contents.length()
					&& StringUtils.containsIgnoreCase(proposal, contents)) {
				list.add(new ContentProposal(proposal));
			}
		}
		return list.toArray(new IContentProposal[list
			.size()]);
	}

}
