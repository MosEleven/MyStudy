git clone

git checkout [-b]

git switch [-c]

git add

git commit

git merge

git cherry-pick：只merge某一次的commit

git branch

git status

git log

git reset：回滚用的

git stash [list] / [apply]：临时保存未提交的改动（这时候就可以切到别的分支临时开发）

### 假如要在dev分支上开发

1. check 到本地的dev分支，pull一个最新的版本
2. checkout一个本地的版本，进行开发
3. 搞完了后切回dev分支，pull一个最新的版本
4. 把本地开发的分支merge进本地的dev分支
5. 把本地的dev分支push到远端
6. 用fetch+merge可能更好