package org.hanuna.gitalk.data;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.VcsCommitMiniDetails;
import com.intellij.vcs.log.VcsLogProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Kirill Likhodedov
 */
public class MiniDetailsGetter extends DataGetter<VcsCommitMiniDetails> {

  MiniDetailsGetter(@NotNull VcsLogDataHolder dataHolder, @NotNull Map<VirtualFile, VcsLogProvider> logProviders) {
    super(dataHolder, logProviders, new VcsCommitCache<VcsCommitMiniDetails>());
  }

  @NotNull
  @Override
  protected List<? extends VcsCommitMiniDetails> readDetails(@NotNull VcsLogProvider logProvider, @NotNull VirtualFile root,
                                                  @NotNull List<String> hashes) throws VcsException {
    return logProvider.readMiniDetails(root, hashes);
  }

}
