# root makefile. Delegate to source subdirs.

PACKAGE=.

SUBDIRS	= src/no/hiof/mobapp/foxhunt/gamebehavior \
	  src/no/hiof/mobapp/foxhunt/server \

all:
	@@for p in $(SUBDIRS); do \
		echo '----- building ' $(PACKAGE)/$$p; \
		make -C $(PACKAGE)/$$p --no-print-directory  all; \
	done

clean:
	@@for p in $(SUBDIRS); do \
		echo 'cleaning ' $(PACKAGE)/$$p; \
		make -C $(PACKAGE)/$$p --no-print-directory clean;\
	done

