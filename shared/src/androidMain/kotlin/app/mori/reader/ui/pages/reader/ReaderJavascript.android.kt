package app.mori.reader.ui.pages.reader

import app.mori.reader.data.audiobook.SasayakiCueRange
import org.json.JSONArray
import org.json.JSONObject

internal fun readerBootstrapScript(
    progress: Double,
    fragment: String?,
    verticalWriting: Boolean,
    isDark: Boolean,
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    characterSpacing: Double,
    continuousMode: Boolean,
    hideFurigana: Boolean,
    sasayakiCues: List<SasayakiCueRange>,
    highlightedSasayakiCueId: String?,
    sasayakiAutoScroll: Boolean,
    sasayakiHighlightEnabled: Boolean,
    sasayakiHighlightColor: String,
): String {
    val writingMode = if (verticalWriting) "vertical-rl" else "horizontal-tb"
    val background = if (isDark) "#101010" else "#fbfaf7"
    val foreground = if (isDark) "#f2f2f2" else "#1b1b1b"
    val safeFontSize = fontSize.coerceIn(16, 40)
    val safeLineHeight = lineHeight.coerceIn(1.0, 2.5)
    val safeHorizontalPadding = horizontalPadding.coerceIn(0, 50).toDouble()
    val safeVerticalPadding = verticalPadding.coerceIn(0, 50).toDouble()
    val safeCharacterSpacing = characterSpacing.coerceIn(-10.0, 10.0)
    val bottomOverlap = if (verticalWriting) safeFontSize else 0
    val columnGap =
        if (verticalWriting) {
            "calc(${safeVerticalPadding}vh + ${bottomOverlap}px)"
        } else {
            "${safeHorizontalPadding}vw"
        }
    val bodyPadding = "${safeVerticalPadding / 2.0}vh ${safeHorizontalPadding / 2.0}vw"
    val bottomPaddingCss =
        if (verticalWriting && bottomOverlap > 0) {
            "padding-bottom: calc(${safeVerticalPadding / 2.0}vh + ${bottomOverlap}px) !important;"
        } else {
            ""
        }
    val pageBreakCss =
        if (avoidPageBreak) {
            """
            p {
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
            """.trimIndent()
        } else {
            ""
        }
    val textSpacingCss =
        """
        line-height: $safeLineHeight !important;
        letter-spacing: ${safeCharacterSpacing / 100.0}em !important;
        """.trimIndent()
    val gridCss =
        if (!justifyText) {
            """
            text-align: start !important;
            hanging-punctuation: allow-end !important;
            line-break: strict !important;
            """.trimIndent()
        } else {
            ""
        }
    val layoutCss =
        if (continuousMode) {
            val perpendicularOverflow =
                if (verticalWriting) {
                    "overflow-y: hidden !important;"
                } else {
                    "overflow-x: hidden !important;"
                }
            val imgWidth = if (verticalWriting) "none" else "${100 - safeHorizontalPadding}vw"
            val imgHeight =
                if (verticalWriting) {
                    "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)"
                } else {
                    "none"
                }
            """
            html, body {
              margin: 0 !important;
              padding: 0 !important;
              min-width: 100vw !important;
              min-height: 100vh !important;
              background: $background !important;
              color: $foreground !important;
              writing-mode: $writingMode !important;
              $perpendicularOverflow
            }
            body {
              box-sizing: border-box !important;
              font-family: serif !important;
              font-size: ${safeFontSize}px !important;
              $textSpacingCss
              padding: $bodyPadding !important;
              $bottomPaddingCss
              $gridCss
            }
            img.block-img {
              max-width: $imgWidth !important;
              max-height: $imgHeight !important;
              width: auto !important;
              height: auto !important;
              display: block !important;
              margin: auto !important;
              object-fit: contain !important;
            }
            svg {
              max-width: $imgWidth !important;
              max-height: $imgHeight !important;
              width: 100% !important;
              height: 100% !important;
              display: block !important;
              margin: auto !important;
            }
        """
        } else {
            """
            html, body {
              margin: 0 !important;
              padding: 0 !important;
              width: var(--page-width, 100vw) !important;
              height: var(--page-height, 100vh) !important;
              overflow: hidden !important;
              background: $background !important;
              color: $foreground !important;
              writing-mode: $writingMode !important;
            }
            body {
              box-sizing: border-box !important;
              font-family: serif !important;
              font-size: ${safeFontSize}px !important;
              $textSpacingCss
              column-width: var(--page-width, 100vw) !important;
              column-gap: $columnGap !important;
              padding: $bodyPadding !important;
              $bottomPaddingCss
              $gridCss
            }
            img.block-img {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
              width: auto !important;
              height: auto !important;
              display: block !important;
              margin: auto !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
              object-fit: contain !important;
            }
            svg {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
              width: 100% !important;
              height: 100% !important;
              display: block !important;
              margin: auto !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
        """
        }.trimIndent()
    val spacerJs =
        if (verticalWriting) {
            """
          var spacer = document.createElement('div');
          spacer.style.height = 'calc(${safeVerticalPadding / 2.0}vh + ${bottomOverlap}px)';
          spacer.style.width = '100%';
          spacer.style.display = 'block';
          spacer.style.breakInside = 'avoid';
          document.body.appendChild(spacer);
        """
        } else {
            """
          var spacer = document.createElement('div');
          spacer.style.height = '100%';
          spacer.style.width = '${safeHorizontalPadding / 2.0}vw';
          spacer.style.display = 'block';
          spacer.style.breakInside = 'avoid';
          document.body.appendChild(spacer);
        """
        }
    val pagingSpacerJs = if (continuousMode) "" else spacerJs
    val restore =
        if (fragment != null) {
            "window.moriReader.jumpToFragment(${fragment.jsString()});"
        } else {
            "window.moriReader.restoreProgress($progress);"
        }
    val sasayakiCueJson = sasayakiCues.toJsonArrayString()
    val highlightedCueJs = highlightedSasayakiCueId?.jsString() ?: "null"
    val cueHighlightColor = sasayakiHighlightColor.takeIf { it.matches(Regex("^#[0-9A-Fa-f]{8}$")) } ?: "#FFC0485C"
    return """
        (function() {
          var viewport = document.querySelector('meta[name="viewport"]');
          if (viewport) viewport.remove();
          viewport = document.createElement('meta');
          viewport.name = 'viewport';
          viewport.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
          document.head.appendChild(viewport);

          var style = document.createElement('style');
          style.textContent = `
            $layoutCss
            figure {
              margin: 0 !important;
              padding: 0 !important;
              break-inside: avoid !important;
              -webkit-column-break-inside: avoid !important;
            }
            $pageBreakCss
            body.mori-image-only img.block-img,
            body.mori-image-only svg {
              max-width: ${100 - safeHorizontalPadding}vw !important;
              max-height: ${if (verticalWriting) "calc(${100 - safeVerticalPadding}vh - ${bottomOverlap * (100 - safeVerticalPadding) / 100.0}px)" else "${100 - safeVerticalPadding}vh"} !important;
            }
            a {
              color: rgba(66, 108, 245, 1) !important;
            }
            rt, rp { user-select: none; }
            ::selection { background: rgba(120, 150, 255, 0.35); }
            ::highlight(mori-selection) {
              background-color: rgba(120, 150, 255, 0.35);
              color: inherit;
            }
            .mori-sasayaki-cue {
              border-radius: 0.16em;
              box-decoration-break: clone;
              -webkit-box-decoration-break: clone;
            }
            .mori-sasayaki-cue-active {
              background: ${if (sasayakiHighlightEnabled) cueHighlightColor else "transparent"};
              color: inherit;
            }
          `;
          document.head.appendChild(style);
          document.documentElement.style.setProperty('--page-height', window.innerHeight + 'px');
          document.documentElement.style.setProperty('--page-width', window.innerWidth + 'px');
          $pagingSpacerJs

          if ($hideFurigana) {
            document.querySelectorAll('rt, rp').forEach(function(el) { el.remove(); });
          }

          document.querySelectorAll('ruby').forEach(function(ruby) {
            Array.from(ruby.childNodes).forEach(function(node) {
              if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
                var span = document.createElement('span');
                span.textContent = node.textContent;
                node.replaceWith(span);
              }
            });
          });

          document.addEventListener('click', function(event) {
            var anchor = event.target && event.target.closest ? event.target.closest('a[href]') : null;
            if (!anchor) return;
            var rawHref = anchor.getAttribute('href') || '';
            var trimmed = rawHref.trim();
            if (!trimmed) return;
            if (/^[a-z][a-z0-9+.-]*:/i.test(trimmed) && !trimmed.toLowerCase().startsWith('file:')) return;
            event.preventDefault();
            AndroidMoriReader.linkActivated(trimmed);
          }, true);

          var hasMatchableText = Array.from(document.body.querySelectorAll('*'))
            .filter(function(el) { return !el.closest('rt, rp, script, style'); })
            .some(function(el) {
              return Array.from(el.childNodes).some(function(node) {
                return node.nodeType === Node.TEXT_NODE &&
                  /[0-9A-Za-z○◯々-〇〻ぁ-ゖゝ-ゞァ-ヺー０-９Ａ-Ｚａ-ｚｦ-ﾝ\p{Radical}\p{Unified_Ideograph}]/u.test(node.textContent || '');
              });
            });
          var mediaCount = document.querySelectorAll('img, svg, image').length;
          if (!hasMatchableText && mediaCount > 0) {
            document.body.classList.add('mori-image-only');
          }

          document.querySelectorAll('svg').forEach(function(svg) {
            svg.classList.add('block-img');
          });

          var imagePromises = Array.from(document.querySelectorAll('img')).map(function(img) {
            return new Promise(function(resolve) {
              var mark = function() {
                var isGaiji = img.classList.contains('gaiji') || img.classList.contains('gaiji-line');
                var parentText = (img.parentElement && img.parentElement.textContent || '').trim();
                var standalone = document.body.classList.contains('mori-image-only') ||
                  parentText.length === 0 ||
                  !!img.closest('figure, .cover, .image, .illustration, .illust');
                var attrWidth = Number.parseInt(img.getAttribute('width') || '0', 10);
                var attrHeight = Number.parseInt(img.getAttribute('height') || '0', 10);
                var isLarge = img.naturalWidth > 256 || img.naturalHeight > 256 || attrWidth > 256 || attrHeight > 256;
                if (!isGaiji && (isLarge || standalone)) {
                  img.classList.add('block-img');
                }
                resolve();
              };
              if (img.complete && img.naturalWidth > 0) mark();
              else {
                img.onload = mark;
                img.onerror = resolve;
              }
            });
          });

          window.moriReader = {
            ttuRegexNegated: /[^0-9A-Za-z○◯々-〇〻ぁ-ゖゝ-ゞァ-ヺー０-９Ａ-Ｚａ-ｚｦ-ﾝ\uF900-\uFAFF\p{Radical}\p{Unified_Ideograph}]+/gimu,
            pageHeight: window.innerHeight,
            pageWidth: window.innerWidth,
            continuousMode: $continuousMode,
            isVertical: function() {
              return window.getComputedStyle(document.body).writingMode === 'vertical-rl';
            },
            isFurigana: function(node) {
              var el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return !!(el && el.closest('rt, rp'));
            },
            normalizeText: function(text) {
              return (text || '').replace(this.ttuRegexNegated, '');
            },
            linkAtPoint: function(x, y) {
              var element = document.elementFromPoint(x, y);
              var anchor = element && element.closest ? element.closest('a[href]') : null;
              if (!anchor) return null;
              var rawHref = anchor.getAttribute('href') || '';
              var trimmed = rawHref.trim();
              if (!trimmed) return null;
              if (/^[a-z][a-z0-9+.-]*:/i.test(trimmed) && !trimmed.toLowerCase().startsWith('file:')) return null;
              return trimmed;
            },
            countChars: function(text) {
              return Array.from(this.normalizeText(text)).length;
            },
            createWalker: function(rootNode) {
              var root = rootNode || document.body;
              var self = this;
              return document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
                acceptNode: function(n) {
                  return self.isFurigana(n) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
                }
              });
            },
            getRect: function(target) {
              var rect = target.getClientRects()[0];
              return rect || target.getBoundingClientRect();
            },
            notifyRestoreComplete: function() {
              AndroidMoriReader.restoreCompleted();
            },
            getScrollContext: function() {
              var vertical = this.isVertical();
              var scrollEl = document.body;
              var pageSize = vertical ? this.pageHeight : this.pageWidth;
              var totalSize = vertical ? scrollEl.scrollHeight : scrollEl.scrollWidth;
              var maxScroll = Math.max(0, totalSize - pageSize);
              return { vertical: vertical, scrollEl: scrollEl, pageSize: pageSize, maxScroll: maxScroll };
            },
            setScrollOffset: function(context, scroll) {
              var clampedScroll = Math.min(Math.max(0, scroll), context.maxScroll);
              if (context.vertical) context.scrollEl.scrollTop = clampedScroll;
              else context.scrollEl.scrollLeft = clampedScroll;
              return clampedScroll;
            },
            alignToPage: function(context, anchor) {
              if (context.pageSize <= 0) return 0;
              var pageIndex = Math.floor(Math.max(0, anchor) / context.pageSize);
              return Math.min(Math.max(0, pageIndex * context.pageSize), context.maxScroll);
            },
            contentEndOffset: function(context) {
              var currentScroll = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
              var maxEnd = 0;
              var walker = this.createWalker();
              var node;

              while (node = walker.nextNode()) {
                if (this.countChars(node.textContent) <= 0) continue;
                var range = document.createRange();
                range.selectNodeContents(node);
                Array.from(range.getClientRects()).forEach(function(rect) {
                  if (rect.width <= 0 || rect.height <= 0) return;
                  var end = (context.vertical ? rect.bottom : rect.right) + currentScroll;
                  maxEnd = Math.max(maxEnd, end);
                });
              }

              document.querySelectorAll('img, svg, video, canvas').forEach(function(element) {
                var rect = element.getBoundingClientRect();
                if (rect.width <= 0 || rect.height <= 0) return;
                var end = (context.vertical ? rect.bottom : rect.right) + currentScroll;
                maxEnd = Math.max(maxEnd, end);
              });

              return maxEnd > 0 ? maxEnd : context.maxScroll + context.pageSize;
            },
            lastContentPageScroll: function(context) {
              if (context.pageSize <= 0) return 0;
              var contentEnd = Math.max(0, this.contentEndOffset(context) - 1);
              var lastPage = Math.floor(contentEnd / context.pageSize) * context.pageSize;
              return Math.min(Math.max(0, lastPage), context.maxScroll);
            },
            maxScroll: function() {
              if (this.isVertical()) return Math.max(0, document.body.scrollHeight - window.innerHeight);
              return Math.max(0, document.body.scrollWidth - window.innerWidth);
            },
            currentScroll: function() {
              return this.isVertical() ? document.body.scrollTop : document.body.scrollLeft;
            },
            setScroll: function(value) {
              var max = this.maxScroll();
              var target = Math.max(0, Math.min(max, value));
              if (this.isVertical()) document.body.scrollTop = target;
              else document.body.scrollLeft = target;
              return target;
            },
            scrollToContinuousStart: function() {
              if (this.isVertical()) {
                window.scrollTo(document.body.scrollWidth, 0);
              } else {
                window.scrollTo(0, 0);
              }
            },
            scrollToContinuousEnd: function() {
              if (this.isVertical()) {
                window.scrollTo(0, 0);
              } else {
                window.scrollTo(0, document.body.scrollHeight);
              }
            },
            registerSnapScroll: function(initialScroll) {
              if (window.snapScrollRegistered) return;
              window.snapScrollRegistered = true;
              window.lastPageScroll = initialScroll;
              var self = this;
              document.body.addEventListener('scroll', function() {
                if (window.programmaticPageScroll) return;
                var vertical = self.isVertical();
                var pageSize = vertical ? self.pageHeight : self.pageWidth;
                var current = vertical ? document.body.scrollTop : document.body.scrollLeft;
                var snapped = Math.round(current / pageSize) * pageSize;
                if (Math.abs(current - snapped) > 1) {
                  if (vertical) document.body.scrollTop = window.lastPageScroll;
                  else document.body.scrollLeft = window.lastPageScroll;
                } else {
                  window.lastPageScroll = snapped;
                }
              }, { passive: true });
            },
            calculateProgress: function() {
              var vertical = this.isVertical();
              var walker = this.createWalker();
              var totalChars = 0;
              var exploredChars = 0;
              var node;

              while (node = walker.nextNode()) {
                var nodeLen = this.countChars(node.textContent);
                totalChars += nodeLen;

                if (nodeLen > 0) {
                  var range = document.createRange();
                  range.selectNodeContents(node);
                  var rect = this.getRect(range);
                  var explored = this.continuousMode
                    ? (vertical ? rect.left > window.innerWidth : rect.bottom < 0)
                    : ((vertical ? rect.top : rect.left) < 0);
                  if (explored) {
                    exploredChars += nodeLen;
                  }
                }
              }

              return totalChars > 0 ? exploredChars / totalChars : 0;
            },
            restoreProgress: async function(progress) {
              await document.fonts.ready;
              if (this.continuousMode) {
                if (progress <= 0) {
                  this.scrollToContinuousStart();
                  requestAnimationFrame(() => this.scrollToContinuousStart());
                  this.notifyRestoreComplete();
                  return;
                }

                if (progress >= 0.99) {
                  this.scrollToContinuousEnd();
                  requestAnimationFrame(() => {
                    this.scrollToContinuousEnd();
                    requestAnimationFrame(() => this.notifyRestoreComplete());
                  });
                  return;
                }

                var vertical = this.isVertical();
                var walker = this.createWalker();
                var totalChars = 0;
                var node;

                while (node = walker.nextNode()) {
                  totalChars += this.countChars(node.textContent);
                }

                if (totalChars <= 0) {
                  this.notifyRestoreComplete();
                  return;
                }

                var targetCharCount = Math.ceil(totalChars * progress);
                var runningSum = 0;
                var targetNode = null;

                walker = this.createWalker();
                while (node = walker.nextNode()) {
                  runningSum += this.countChars(node.textContent);
                  targetNode = node;
                  if (runningSum > targetCharCount) break;
                }

                if (targetNode && targetNode.parentElement) {
                  targetNode.parentElement.scrollIntoView({
                    block: progress >= 0.999999 ? 'end' : 'start',
                    inline: 'nearest',
                    behavior: 'instant'
                  });
                }

                requestAnimationFrame(() => {
                  requestAnimationFrame(() => this.notifyRestoreComplete());
                });
                return;
              }
              var context = this.getScrollContext();

              if (context.pageSize <= 0) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              if (progress <= 0) {
                this.setScrollOffset(context, 0);
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              if (progress >= 0.99) {
                var lastPage = this.lastContentPageScroll(context);
                this.setScrollOffset(context, lastPage);
                requestAnimationFrame(() => {
                  this.setScrollOffset(context, lastPage);
                  this.registerSnapScroll(lastPage);
                  requestAnimationFrame(() => this.notifyRestoreComplete());
                });
                return;
              }

              var walker = this.createWalker();
              var totalChars = 0;
              var node;

              while (node = walker.nextNode()) {
                totalChars += this.countChars(node.textContent);
              }

              if (totalChars <= 0) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return;
              }

              var targetCharCount = Math.ceil(totalChars * progress);
              var runningSum = 0;
              var targetNode = null;

              walker = this.createWalker();
              while (node = walker.nextNode()) {
                runningSum += this.countChars(node.textContent);
                if (runningSum > targetCharCount) {
                  targetNode = node;
                  break;
                }
              }

              if (targetNode) {
                var range = document.createRange();
                range.setStart(targetNode, 0);
                range.setEnd(targetNode, 1);
                var rect = this.getRect(range);
                var anchor = (context.vertical ? rect.top : rect.left) + (context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft);
                var targetScroll = this.alignToPage(context, anchor);

                this.setScrollOffset(context, targetScroll);
                requestAnimationFrame(() => {
                  this.setScrollOffset(context, targetScroll);
                  this.registerSnapScroll(targetScroll);
                });
              } else {
                this.registerSnapScroll(0);
              }

              requestAnimationFrame(() => {
                requestAnimationFrame(() => this.notifyRestoreComplete());
              });
            },
            jumpToFragment: async function(fragment) {
              await document.fonts.ready;
              var rawFragment = (fragment || '').trim();
              var target = rawFragment && (document.getElementById(rawFragment) || document.getElementsByName(rawFragment)[0]);

              if (this.continuousMode) {
                if (!target) {
                  this.notifyRestoreComplete();
                  return false;
                }
                target.scrollIntoView();
                requestAnimationFrame(() => {
                  requestAnimationFrame(() => {
                    AndroidMoriReader.progressSaved(this.calculateProgress());
                    this.notifyRestoreComplete();
                  });
                });
                return true;
              }

              var context = this.getScrollContext();
              if (context.pageSize <= 0 || !target) {
                this.registerSnapScroll(0);
                this.notifyRestoreComplete();
                return false;
              }

              var rect = this.getRect(target);
              var currentScroll = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
              var anchor = (context.vertical ? rect.top : rect.left) + currentScroll;
              var targetScroll = this.alignToPage(context, anchor);

              this.setScrollOffset(context, targetScroll);

              requestAnimationFrame(() => {
                this.setScrollOffset(context, targetScroll);
                this.registerSnapScroll(targetScroll);
                requestAnimationFrame(() => {
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  this.notifyRestoreComplete();
                });
              });

              return true;
            },
            continuousBoundary: function(direction) {
              if (!this.continuousMode) return 'none';
              var max = this.maxScroll();
              var current = this.currentScroll();
              if (direction === 'forward' && current >= max - 2) return 'forward';
              if (direction === 'backward' && current <= 2) return 'backward';
              return 'none';
            },
            paginate: function(direction) {
              var context = this.getScrollContext();
              var page = context.pageSize;
              if (page <= 0) return 'limit';

              if (direction === 'forward') {
                var maxAligned = this.lastContentPageScroll(context);
                var current = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
                if ((current + page) <= (maxAligned + 1)) {
                  var forwardTarget = Math.round((current + page) / page) * page;
                  window.lastPageScroll = forwardTarget;
                  this.setScrollOffset(context, forwardTarget);
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  return 'scrolled';
                }
                return 'limit';
              } else {
                var currentBack = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
                if (currentBack > 0) {
                  var backwardTarget = Math.round((currentBack - page) / page) * page;
                  window.lastPageScroll = backwardTarget;
                  this.setScrollOffset(context, backwardTarget);
                  AndroidMoriReader.progressSaved(this.calculateProgress());
                  return 'scrolled';
                }
                return 'limit';
              }
            }
          };

          window.moriSasayaki = {
            cues: [],
            activeCueId: null,
            isIgnored: function(node) {
              var el = node && node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return !!(el && el.closest('rt, rp, script, style'));
            },
            createWalker: function(rootNode) {
              var self = this;
              return document.createTreeWalker(rootNode || document.body, NodeFilter.SHOW_TEXT, {
                acceptNode: function(node) {
                  return self.isIgnored(node) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
                }
              });
            },
            matchableSegments: function(text) {
              var segments = [];
              var normalized = '';
              var raw = 0;
              while (raw < text.length) {
                var cp = text.codePointAt(raw);
                if (cp === undefined) break;
                var ch = String.fromCodePoint(cp);
                var next = raw + ch.length;
                if (window.moriReader.normalizeText(ch).length > 0) {
                  segments.push({ rawStart: raw, rawEnd: next, normalizedStart: normalized.length });
                  normalized += ch;
                }
                raw = next;
              }
              return { normalized: normalized, segments: segments };
            },
            collectTextNodes: function() {
              var walker = this.createWalker(document.body);
              var nodes = [];
              var offset = 0;
              var node;
              while (node = walker.nextNode()) {
                var info = this.matchableSegments(node.textContent || '');
                if (info.normalized.length > 0) {
                  nodes.push({ node: node, start: offset, end: offset + info.normalized.length, segments: info.segments });
                  offset += info.normalized.length;
                }
              }
              return nodes;
            },
            normalizedOffsetFor: function(targetNode, rawOffset) {
              var walker = this.createWalker(document.body);
              var offset = 0;
              var node;
              while (node = walker.nextNode()) {
                var text = node.textContent || '';
                var info = this.matchableSegments(text);
                if (node === targetNode) {
                  var local = 0;
                  for (var i = 0; i < info.segments.length; i++) {
                    if (info.segments[i].rawStart >= rawOffset) break;
                    local++;
                  }
                  return offset + local;
                }
                offset += info.normalized.length;
              }
              return null;
            },
            wrapTextNode: function(node, ranges) {
              ranges.sort(function(a, b) { return b.start - a.start; }).forEach(function(item) {
                if (!node.parentNode || item.end <= item.start) return;
                var text = node;
                var after = text.splitText(item.end);
                var middle = text.splitText(item.start);
                var span = document.createElement('span');
                span.className = 'mori-sasayaki-cue';
                span.dataset.sasayakiCueId = item.cueId;
                middle.parentNode.insertBefore(span, middle);
                span.appendChild(middle);
                node = text;
                void after;
              });
            },
            applySasayakiCues: function(cues) {
              document.querySelectorAll('.mori-sasayaki-cue').forEach(function(span) {
                span.replaceWith(document.createTextNode(span.textContent || ''));
              });
              document.body.normalize();
              this.cues = Array.isArray(cues) ? cues : [];
              this.activeCueId = null;
              if (!this.cues.length) return 0;
              var nodes = this.collectTextNodes();
              var nodeRanges = nodes.map(function(item) {
                return { node: item.node, ranges: [] };
              });
              var wrapped = 0;
              this.cues.forEach(function(cue) {
                var start = Number(cue.start);
                var end = start + Number(cue.length);
                if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) return;
                nodes.forEach(function(item, itemIndex) {
                  if (item.end <= start || item.start >= end) return;
                  var rawRanges = [];
                  item.segments.forEach(function(segment) {
                    var absolute = item.start + segment.normalizedStart;
                    if (absolute >= start && absolute < end) {
                      rawRanges.push({ start: segment.rawStart, end: segment.rawEnd });
                    }
                  });
                  if (!rawRanges.length) return;
                  var merged = [];
                  rawRanges.forEach(function(range) {
                    var last = merged[merged.length - 1];
                    if (last && last.end === range.start) last.end = range.end;
                    else merged.push({ start: range.start, end: range.end });
                  });
                  merged.forEach(function(range) {
                    nodeRanges[itemIndex].ranges.push({
                      start: range.start,
                      end: range.end,
                      cueId: String(cue.id)
                    });
                    wrapped++;
                  });
                });
              });
              nodeRanges.forEach(function(item) {
                if (item.ranges.length) window.moriSasayaki.wrapTextNode(item.node, item.ranges);
              });
              return wrapped;
            },
            highlightSasayakiCue: function(id) {
              this.clearSasayakiCue();
              if (id == null) return false;
              var selector = '.mori-sasayaki-cue[data-sasayaki-cue-id="' + String(id).replace(/"/g, '\\"') + '"]';
              var spans = Array.from(document.querySelectorAll(selector));
              spans.forEach(function(span) { span.classList.add('mori-sasayaki-cue-active'); });
              if ($sasayakiAutoScroll && spans.length) {
                this.scrollToCue(spans[0]);
              }
              this.activeCueId = spans.length ? String(id) : null;
              return spans.length > 0;
            },
            scrollToCue: function(target) {
              if (!target) return false;
              if (window.moriReader.continuousMode) {
                target.scrollIntoView({
                  block: 'center',
                  inline: 'center',
                  behavior: 'instant'
                });
                AndroidMoriReader.progressSaved(window.moriReader.calculateProgress());
                return true;
              }

              var context = window.moriReader.getScrollContext();
              if (context.pageSize <= 0) return false;
              var rect = window.moriReader.getRect(target);
              var currentScroll = context.vertical ? context.scrollEl.scrollTop : context.scrollEl.scrollLeft;
              var anchor = (context.vertical ? rect.top : rect.left) + currentScroll;
              var targetScroll = window.moriReader.alignToPage(context, anchor);
              window.lastPageScroll = targetScroll;
              window.moriReader.setScrollOffset(context, targetScroll);
              requestAnimationFrame(function() {
                window.moriReader.setScrollOffset(context, targetScroll);
                AndroidMoriReader.progressSaved(window.moriReader.calculateProgress());
              });
              return true;
            },
            clearSasayakiCue: function() {
              document.querySelectorAll('.mori-sasayaki-cue-active').forEach(function(span) {
                span.classList.remove('mori-sasayaki-cue-active');
              });
              this.activeCueId = null;
            }
          };
          window.applySasayakiCues = function(cues) { return window.moriSasayaki.applySasayakiCues(cues); };
          window.highlightSasayakiCue = function(id) { return window.moriSasayaki.highlightSasayakiCue(id); };
          window.clearSasayakiCue = function() { return window.moriSasayaki.clearSasayakiCue(); };

          window.moriSelection = {
            selection: null,
            scanDelimiters: '。、！？…‥「」『』（）()【】〈〉《》〔〕｛｝{}［］[]・：；:;，,.─\n\r',
            sentenceDelimiters: '。！？.!?\n\r',
            trailingSentenceChars: '。、！？…‥」』）)】〉》〕｝}］]',
            brackets: {'「':'」', '『':'』', '（':'）', '(' :')', '【':'】', '〈':'〉', '《':'》', '〔':'〕', '｛':'｝', '{':'}', '［':'］', '[':']'},
            isBoundary: function(ch) {
              return !ch || /^[\s\u3000]$/.test(ch) || this.scanDelimiters.indexOf(ch) >= 0;
            },
            isFurigana: function(node) {
              var el = node && node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return !!(el && el.closest('rt, rp'));
            },
            findTextScope: function(node) {
              var el = node && node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
              return (el && el.closest('p, li, blockquote, div, section, article')) || document.body;
            },
            createWalker: function(rootNode) {
              var self = this;
              return document.createTreeWalker(rootNode || document.body, NodeFilter.SHOW_TEXT, {
                acceptNode: function(node) {
                  return self.isFurigana(node) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT;
                }
              });
            },
            rangeContainsPoint: function(range, x, y) {
              var rects = range.getClientRects();
              for (var i = 0; i < rects.length; i++) {
                var rect = rects[i];
                if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) return true;
              }
              var fallback = range.getBoundingClientRect();
              return x >= fallback.left && x <= fallback.right && y >= fallback.top && y <= fallback.bottom;
            },
            caretRange: function(x, y) {
              if (document.caretPositionFromPoint) {
                var pos = document.caretPositionFromPoint(x, y);
                if (!pos) return null;
                var range = document.createRange();
                range.setStart(pos.offsetNode, pos.offset);
                range.collapse(true);
                return range;
              }
              if (document.caretRangeFromPoint) {
                var webkitRange = document.caretRangeFromPoint(x, y);
                if (webkitRange) return webkitRange;
              }
              var element = document.elementFromPoint(x, y);
              var scope = element ? this.findTextScope(element) : document.body;
              var walker = this.createWalker(scope);
              var probe = document.createRange();
              var node;
              while (node = walker.nextNode()) {
                var text = node.textContent || '';
                for (var i = 0; i < text.length; i++) {
                  probe.setStart(node, i);
                  probe.setEnd(node, i + 1);
                  if (this.rangeContainsPoint(probe, x, y)) {
                    probe.collapse(true);
                    return probe;
                  }
                }
              }
              return null;
            },
            characterAtPoint: function(x, y) {
              var range = this.caretRange(x, y);
              if (!range || range.startContainer.nodeType !== Node.TEXT_NODE) return null;
              var node = range.startContainer;
              if (this.isFurigana(node)) return null;
              var text = node.textContent || '';
              var caret = range.startOffset;
              var offsets = [caret, caret - 1, caret + 1];
              for (var i = 0; i < offsets.length; i++) {
                var offset = offsets[i];
                if (offset < 0 || offset >= text.length) continue;
                var charRange = document.createRange();
                charRange.setStart(node, offset);
                charRange.setEnd(node, offset + 1);
                if (this.rangeContainsPoint(charRange, x, y)) {
                  return this.isBoundary(text[offset]) ? null : { node: node, offset: offset };
                }
              }
              return null;
            },
            sentenceAt: function(node, offset) {
              var scope = this.findTextScope(node);
              var walker = this.createWalker(scope);
              var before = [];
              var current = node;
              var limit = offset;
              walker.currentNode = node;
              while (current) {
                var text = current.textContent || '';
                var foundStart = false;
                for (var i = limit - 1; i >= 0; i--) {
                  if (this.sentenceDelimiters.indexOf(text[i]) >= 0) {
                    before.push(text.slice(i + 1, limit));
                    foundStart = true;
                    break;
                  }
                }
                if (foundStart) break;
                before.push(text.slice(0, limit));
                current = walker.previousNode();
                if (current) limit = (current.textContent || '').length;
              }

              var after = [];
              current = node;
              var start = offset;
              walker.currentNode = node;
              while (current) {
                var content = current.textContent || '';
                var foundEnd = false;
                for (var j = start; j < content.length; j++) {
                  if (this.sentenceDelimiters.indexOf(content[j]) >= 0) {
                    var end = j + 1;
                    while (end < content.length && this.trailingSentenceChars.indexOf(content[end]) >= 0) end++;
                    after.push(content.slice(start, end));
                    foundEnd = true;
                    break;
                  }
                }
                if (foundEnd) break;
                after.push(content.slice(start));
                current = walker.nextNode();
                start = 0;
              }

              var sentence = (before.reverse().join('') + after.join('')).trim();
              var openBrackets = Object.keys(this.brackets);
              var closeBrackets = Object.values(this.brackets);
              var stack = [];
              var unmatchedClose = [];

              for (var index = 0; index < sentence.length; index++) {
                var ch = sentence[index];
                if (openBrackets.indexOf(ch) >= 0) {
                  stack.push(ch);
                } else if (closeBrackets.indexOf(ch) >= 0) {
                  if (stack.length > 0 && this.brackets[stack[stack.length - 1]] === ch) {
                    stack.pop();
                  } else {
                    unmatchedClose.push(ch);
                  }
                }
              }

              var startSlice = 0;
              while (stack.length > 0 && startSlice < sentence.length - 1) {
                if (stack[0] === sentence[startSlice]) {
                  stack.shift();
                } else {
                  break;
                }
                startSlice++;
              }

              var endSlice = sentence.length - 1;
              var endIndex = sentence.length - 1;
              while (unmatchedClose.length > 0 && endIndex > startSlice) {
                if (unmatchedClose[unmatchedClose.length - 1] === sentence[endIndex]) {
                  unmatchedClose.pop();
                  endSlice = endIndex - 1;
                } else if (this.sentenceDelimiters.indexOf(sentence[endIndex]) < 0) {
                  break;
                }
                endIndex--;
              }

              return sentence.slice(startSlice, endSlice + 1).trim();
            },
            clearSelection: function() {
              window.getSelection().removeAllRanges();
              this.clearCustomHighlight();
              this.selection = null;
            },
            clearCustomHighlight: function() {
              if (window.CSS && CSS.highlights) CSS.highlights.delete('mori-selection');
            },
            highlightSelection: function(charCount) {
              var ranges = (this.selection && this.selection.ranges) || [];
              window.getSelection().removeAllRanges();
              this.clearCustomHighlight();
              if (!ranges.length) return;
              if (!(window.CSS && CSS.highlights && window.Highlight)) return;
              var remaining = Math.max(0, charCount || 0);
              var highlights = [];
              for (var rIndex = 0; rIndex < ranges.length && remaining > 0; rIndex++) {
                var item = ranges[rIndex];
                var end = item.start;
                var content = item.node.textContent || '';
                while (end < item.end && remaining > 0) {
                  var codePoint = content.codePointAt(end);
                  if (codePoint === undefined) break;
                  end += String.fromCodePoint(codePoint).length;
                  remaining--;
                }
                if (end > item.start) {
                  var range = document.createRange();
                  range.setStart(item.node, item.start);
                  range.setEnd(item.node, end);
                  highlights.push(range);
                }
              }
              if (highlights.length) CSS.highlights.set('mori-selection', new Highlight(...highlights));
            },
            selectionRect: function(x, y) {
              if (!this.selection || !this.selection.ranges.length) return null;
              var first = this.selection.ranges[0];
              var range = document.createRange();
              range.setStart(first.node, first.start);
              range.setEnd(first.node, Math.min(first.end, first.start + 1));
              var rects = Array.from(range.getClientRects());
              var rect = rects.find(function(item) {
                return x >= item.left && x <= item.right && y >= item.top && y <= item.bottom;
              }) || range.getBoundingClientRect();
              var normalizedOffset = window.moriSasayaki
                ? window.moriSasayaki.normalizedOffsetFor(first.node, first.start)
                : null;
              return { x: rect.x, y: rect.y, width: rect.width, height: rect.height, normalizedOffset: normalizedOffset };
            },
            selectText: function(x, y, maxLength) {
              var hit = this.characterAtPoint(x, y);
              if (!hit) {
                this.clearSelection();
                return null;
              }
              if (this.selection && this.selection.node === hit.node && this.selection.offset === hit.offset) {
                this.clearSelection();
                return null;
              }
              this.clearSelection();
              var scope = this.findTextScope(hit.node);
              var walker = this.createWalker(scope);
              var node = hit.node;
              var offset = hit.offset;
              var selected = '';
              var ranges = [];
              walker.currentNode = node;

              while (node && selected.length < maxLength) {
                var text = node.textContent || '';
                var startOffset = offset;
                while (offset < text.length && selected.length < maxLength && !this.isBoundary(text[offset])) {
                  selected += text[offset];
                  offset++;
                }
                if (offset > startOffset) {
                  ranges.push({ node: node, start: startOffset, end: offset });
                }
                if (offset < text.length || selected.length >= maxLength) break;
                node = walker.nextNode();
                offset = 0;
              }
              if (!selected) {
                this.clearSelection();
                return null;
              }
              this.selection = {
                node: hit.node,
                offset: hit.offset,
                ranges: ranges,
                text: selected
              };
              AndroidMoriReader.textSelected(JSON.stringify({
                text: selected,
                sentence: this.sentenceAt(hit.node, hit.offset),
                rect: this.selectionRect(x, y)
              }));
              return selected;
            }
          };

          Promise.all(imagePromises).then(function() {
            return new Promise(function(resolve) { setTimeout(resolve, 50); });
          }).then(function() {
            window.moriSasayaki.applySasayakiCues($sasayakiCueJson);
            if ($highlightedCueJs !== null) window.moriSasayaki.highlightSasayakiCue($highlightedCueJs);
            $restore
          });
        })();
        """.trimIndent()
}

internal fun List<SasayakiCueRange>.toJsonArrayString(): String {
    val array = JSONArray()
    forEach { cue ->
        array.put(
            JSONObject()
                .put("id", cue.id)
                .put("start", cue.start)
                .put("length", cue.length),
        )
    }
    return array.toString()
}

internal fun String.jsString(): String =
    buildString {
        append('\'')
        this@jsString.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
        append('\'')
    }
