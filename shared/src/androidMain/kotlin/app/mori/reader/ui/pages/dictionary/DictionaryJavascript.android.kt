package app.mori.reader.ui.pages.dictionary

internal fun dictionaryJs(audioButtonTitle: String): String =
    """
const KANJI_RANGE = '\u4E00-\u9FFF\u3400-\u4DBF\uF900-\uFAFF\u3005';
const KANJI_PATTERN = new RegExp(`[${'$'}{KANJI_RANGE}]`);
const KANJI_SEGMENT_PATTERN = new RegExp(`[${'$'}{KANJI_RANGE}]+|[^${'$'}{KANJI_RANGE}]+`, 'g');
const SMALL_KANA_SET = new Set('ぁぃぅぇぉゃゅょゎァィゥェォャュョヮ');
const POS_TAGS = new Set(['n', 'adj-i', 'adj-na', 'adj-no', 'v1', 'vk', 'vs', 'vs-i', 'vs-s', 'vz', 'vi', 'vt']);
const NUMERIC_TAG = /^\d+${'$'}/;
const DEFAULT_HARMONIC_RANK = '9999999';
const COMPACT_GLOSSARIES_ANKI = `.yomitan-glossary ul[data-sc-content="glossary"] > li:not(:first-child)::before, .yomitan-glossary .glossary-list > li:not(:first-child)::before { white-space: pre-wrap; content: " | "; display: inline; color: rgb(119, 119, 119); }
.yomitan-glossary ul[data-sc-content="glossary"] > li, .yomitan-glossary .glossary-list > li { display: inline; }
.yomitan-glossary ul[data-sc-content="glossary"], .yomitan-glossary .glossary-list { display: inline; list-style: none; padding-left: 0px; }`;
let audioUrls = {};
let selectedDictionaries = {};
let currentDictionaryMedia = null;
const backStack = [];
const forwardStack = [];

function el(tag, props = {}, children = []) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props)) {
    if (key === 'styleText') node.style.cssText = value;
    else if (key in node) node[key] = value;
    else node.setAttribute(key, value);
  }
  node.append(...children);
  return node;
}
function notifyNavigationState() {
  try { AndroidHoshi.updateNavigationState(backStack.length, forwardStack.length); } catch {}
}
function toHiragana(text) { return text.replace(/[\u30A1-\u30F6]/g, ch => String.fromCharCode(ch.charCodeAt(0) - 0x60)); }
function toKebabCase(str) { return str.replace(/([A-Z])/g, (_, c, i) => (i ? '-' : '') + c.toLowerCase()); }
function parseTags(tags) { return (tags || '').split(/\s+/).filter(Boolean); }
function isPartOfSpeech(tag) { return POS_TAGS.has(tag) || tag.startsWith('v5'); }
function isStringPartiallyJapanese(text) { return !!text && /[\u3000-\u30ff\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff00-\uffef]/.test(text); }
function isStringPartiallyChinese(text) { return !!text && (KANJI_PATTERN.test(text) || /[\u3100-\u312f\u31a0-\u31bf]/.test(text)); }
function getLanguageFromText(text, language) {
  const partiallyJapanese = isStringPartiallyJapanese(text);
  const partiallyChinese = isStringPartiallyChinese(text);
  if (!['zh', 'yue'].includes(language ?? '')) {
    if (partiallyJapanese) return 'ja';
    if (partiallyChinese) return 'zh';
  }
  return language ?? null;
}
function setStructuredContentElementStyle(element, style) {
  for (const [property, value] of Object.entries(style || {})) {
    if ((property === 'marginTop' || property === 'marginLeft' || property === 'marginRight' || property === 'marginBottom') && typeof value === 'number') element.style[property] = `${'$'}{value}em`;
    else element.style[property] = value;
  }
}
function constructDictCss(css, dictName, rootSelector = '.glossary-group') {
  if (!css) return '';
  const prefix = `${'$'}{rootSelector} [data-dictionary="${'$'}{dictName}"]`;
  const parts = [];
  let i = 0;
  while (i < css.length) {
    while (i < css.length && /\s/.test(css[i])) parts.push(css[i++]);
    if (css.slice(i, i + 2) === '/*') {
      const end = css.indexOf('*/', i + 2);
      if (end === -1) break;
      parts.push(css.slice(i, end + 2));
      i = end + 2;
      continue;
    }
    const bracePos = css.indexOf('{', i);
    if (bracePos === -1) break;
    const selectorPart = css.slice(i, bracePos);
    const selectors = selectorPart.split(',').map(s => {
      const trimmed = s.trim();
      if (!trimmed) return '';
      if (trimmed.startsWith('&')) return s;
      return `${'$'}{prefix} ${'$'}{trimmed}`;
    });
    parts.push(selectors.join(', '), ' {');
    i = bracePos + 1;
    let depth = 1;
    const blockStart = i;
    while (i < css.length && depth > 0) {
      if (css[i] === '{') depth++;
      else if (css[i] === '}') depth--;
      i++;
    }
    const blockContent = css.slice(blockStart, i - 1);
    if (blockContent.includes('{')) {
      let pos = 0;
      let properties = '';
      let nestedRules = '';
      while (pos < blockContent.length) {
        while (pos < blockContent.length && /\s/.test(blockContent[pos])) pos++;
        if (pos >= blockContent.length) break;
        const nextSemi = blockContent.indexOf(';', pos);
        const nextBrace = blockContent.indexOf('{', pos);
        if (nextBrace !== -1 && (nextSemi === -1 || nextBrace < nextSemi)) {
          let nestedDepth = 1;
          let nestedEnd = nextBrace + 1;
          while (nestedEnd < blockContent.length && nestedDepth > 0) {
            if (blockContent[nestedEnd] === '{') nestedDepth++;
            else if (blockContent[nestedEnd] === '}') nestedDepth--;
            nestedEnd++;
          }
          nestedRules += blockContent.slice(pos, nestedEnd);
          pos = nestedEnd;
        } else if (nextSemi !== -1) {
          properties += blockContent.slice(pos, nextSemi + 1);
          pos = nextSemi + 1;
        } else {
          properties += blockContent.slice(pos);
          break;
        }
      }
      parts.push(properties);
      if (nestedRules) parts.push(constructDictCss(nestedRules, dictName, rootSelector));
    } else {
      parts.push(blockContent);
    }
    parts.push('}');
  }
  return parts.join('');
}
function getMediaFilename(dictionary, path) {
  if (!currentDictionaryMedia || !dictionary || !path) return path || '';
  const key = `${'$'}{dictionary}:${'$'}{path}`;
  if (!currentDictionaryMedia.has(key)) {
    const extension = path.split('.').pop() || 'bin';
    currentDictionaryMedia.set(key, {
      dictionary,
      path,
      filename: `mori_dict_${'$'}{currentDictionaryMedia.size}.${'$'}{extension}`,
    });
  }
  return currentDictionaryMedia.get(key).filename;
}
function createDefinitionImage(data, dictionary, exporting = false) {
  const {
    path,
    width = 100,
    height = 100,
    preferredWidth,
    preferredHeight,
    title,
    pixelated,
    imageRendering,
    appearance,
    background,
    collapsed,
    collapsible,
    verticalAlign,
    border,
    borderRadius,
    sizeUnits,
    data: nodeData,
  } = data;
  if (!path) return document.createTextNode('');
  const hasPreferredWidth = typeof preferredWidth === 'number';
  const hasPreferredHeight = typeof preferredHeight === 'number';
  const hasDimensions = hasPreferredWidth || hasPreferredHeight || typeof data.width === 'number' || typeof data.height === 'number';
  const invAspectRatio = hasPreferredWidth && hasPreferredHeight ? preferredHeight / preferredWidth : height / width;
  const usedWidth = hasPreferredWidth ? preferredWidth : (hasPreferredHeight ? preferredHeight / invAspectRatio : width);
  const node = exporting
    ? el('span', { className: 'gloss-image-link' })
    : el('a', { className: 'gloss-image-link', target: '_blank', rel: 'noreferrer noopener' });
  const imageContainer = el('span', { className: 'gloss-image-container' });
  const aspectRatioSizer = el('span', { className: 'gloss-image-sizer' });
  const imageBackground = el('span', { className: 'gloss-image-background' });
  const overlay = el('span', { className: 'gloss-image-container-overlay' });
  imageContainer.append(aspectRatioSizer, imageBackground, overlay);
  node.appendChild(imageContainer);
  node.dataset.path = path;
  node.dataset.dictionary = dictionary;
  node.dataset.hasAspectRatio = 'true';
  node.dataset.imageRendering = typeof imageRendering === 'string' ? imageRendering : (pixelated ? 'pixelated' : 'auto');
  node.dataset.appearance = typeof appearance === 'string' ? appearance : 'auto';
  node.dataset.background = typeof background === 'boolean' ? `${'$'}{background}` : 'true';
  node.dataset.collapsed = typeof collapsed === 'boolean' ? `${'$'}{collapsed}` : 'false';
  node.dataset.collapsible = typeof collapsible === 'boolean' ? `${'$'}{collapsible}` : 'true';
  if (typeof verticalAlign === 'string') node.dataset.verticalAlign = verticalAlign;
  if (typeof sizeUnits === 'string') node.dataset.sizeUnits = sizeUnits;
  aspectRatioSizer.style.paddingTop = `${'$'}{invAspectRatio * 100}%`;
  if (typeof border === 'string') imageContainer.style.border = border;
  if (typeof borderRadius === 'string') imageContainer.style.borderRadius = borderRadius;
  imageContainer.style.width = `${'$'}{usedWidth}em`;
  if (typeof title === 'string') imageContainer.title = title;
  const filename = exporting && (window.useAnkiConnect || window.embedMedia) ? getMediaFilename(dictionary, path) : null;
  const alt = nodeData?.alt || title || '';
  const img = filename || !exporting
    ? el('img', {
        className: 'gloss-image',
        alt,
        src: filename || `image://?dictionary=${'$'}{encodeURIComponent(dictionary)}&path=${'$'}{encodeURIComponent(path)}`,
      })
    : el('span', { className: 'gloss-image', textContent: alt });
  if (exporting && filename) {
    if (sizeUnits === 'em') {
      const emSize = 14;
      const scaleFactor = 2 * window.devicePixelRatio;
      img.width = usedWidth * emSize * scaleFactor;
    } else {
      img.width = usedWidth;
    }
    img.height = img.width * invAspectRatio;
    applyImageStyles(node, imageContainer, aspectRatioSizer, imageBackground, img, filename, appearance, sizeUnits === 'em');
  }
  if (!hasDimensions) {
    img.addEventListener('load', () => {
      if (img.naturalWidth > 0) {
        imageContainer.style.width = `${'$'}{Math.min(img.naturalWidth, window.innerWidth - 20)}px`;
        aspectRatioSizer.style.paddingTop = `${'$'}{(img.naturalHeight / img.naturalWidth) * 100}%`;
      }
    }, { once: true });
  }
  imageContainer.appendChild(img);
  return node;
}
function segmentFurigana(expression, reading) {
  if (!reading || reading === expression) return [[expression, '']];
  const groups = (expression.match(KANJI_SEGMENT_PATTERN) || []).map(text => ({ text, isKana: !KANJI_PATTERN.test(text[0]), textNormalized: !KANJI_PATTERN.test(text[0]) ? toHiragana(text) : null }));
  function walk(read, normalized, index) {
    if (index >= groups.length) return read.length === 0 ? [] : null;
    const group = groups[index];
    if (group.isKana) {
      if (!normalized.startsWith(group.textNormalized)) return null;
      const rest = walk(read.slice(group.text.length), normalized.slice(group.text.length), index + 1);
      return rest ? [{ text: group.text, reading: '' }, ...rest] : null;
    }
    for (let i = read.length; i >= group.text.length; i--) {
      const rest = walk(read.slice(i), normalized.slice(i), index + 1);
      if (rest) return [{ text: group.text, reading: read.slice(0, i) }, ...rest];
    }
    return null;
  }
  const result = walk(reading, toHiragana(reading), 0);
  return result ? result.map(x => [x.text, x.reading]) : [[expression, reading]];
}
function buildFurigana(parent, expression, reading) {
  for (const [text, rubyText] of segmentFurigana(expression, reading)) {
    if (rubyText) parent.appendChild(el('ruby', {}, [document.createTextNode(text), el('rt', { textContent: rubyText })]));
    else parent.appendChild(document.createTextNode(text));
  }
}
function isRubyAnnotationNode(node) {
  let current = node;
  while (current) {
    if (current.nodeType === Node.ELEMENT_NODE && current.tagName === 'RT') return true;
    current = current.parentNode;
  }
  return false;
}
function renderStructuredContent(parent, content, language = null, dictionary = null, exporting = false) {
  if (content == null) return;
  if (typeof content === 'string') {
    content.split(/\r?\n/).forEach((line, i) => {
      if (i > 0) parent.appendChild(document.createElement('br'));
      if (line) {
        if (!language && !parent.hasAttribute('lang')) {
          const detected = getLanguageFromText(line, language);
          if (detected) parent.setAttribute('lang', detected);
        }
        parent.appendChild(document.createTextNode(line));
      }
    });
    return;
  }
  if (typeof content === 'number') {
    parent.appendChild(document.createTextNode(String(content)));
    return;
  }
  if (Array.isArray(content)) {
    const isStringArray = content.every(item => typeof item === 'string');
    const insideSpan = parent.tagName === 'SPAN';
    if (isStringArray && content.length > 1 && !insideSpan) {
      const ul = el('ul', { className: 'glossary-list' });
      content.forEach(child => ul.appendChild(el('li', {}, [document.createTextNode(child)])));
      parent.appendChild(ul);
      return;
    }
    const items = content.map(item => item?.type === 'structured-content' ? item.content : item);
    const isLinkArray = items.every(item => item?.tag === 'a');
    if (isLinkArray && content.length > 1) {
      const ul = el('ul', { className: 'glossary-list' });
      content.forEach(child => {
        const li = el('li');
        renderStructuredContent(li, child, language, dictionary, exporting);
        ul.appendChild(li);
      });
      parent.appendChild(ul);
      return;
    }
    content.forEach(item => renderStructuredContent(parent, item, language, dictionary, exporting));
    return;
  }
  if (content.type === 'structured-content') {
    const container = el('span', { className: 'structured-content' });
    parent.appendChild(container);
    renderStructuredContent(container, content.content, language, dictionary, exporting);
    return;
  }
  if (content.type === 'image' || content.tag === 'img') {
    parent.appendChild(createDefinitionImage(content, dictionary, exporting));
    return;
  }
  const tag = typeof content.tag === 'string' ? content.tag : 'span';
  const node = el(tag, { className: `gloss-sc-${'$'}{tag}` });
  let nextLanguage = language;
  if (content.href) {
    node.href = content.href;
    node.addEventListener('click', async e => {
      e.preventDefault();
      e.stopPropagation();
      const href = content.href;
      if (/^https?:\/\//i.test(href)) {
        AndroidHoshi.openLink(href);
        return;
      }
      const index = href.indexOf('?');
      const query = index < 0 ? null : new URLSearchParams(href.slice(index + 1)).get('query');
      if (!query) {
        AndroidHoshi.openLink(href);
        return;
      }
      try {
        const payload = AndroidHoshi.lookupRedirect?.(query, window.maxResults || 16);
        const result = payload ? JSON.parse(payload) : null;
        redirectToLookup(result, query);
      } catch {
        AndroidHoshi.openLink(href);
      }
    });
  }
  if (content.title) node.setAttribute('title', content.title);
  if (content.lang) {
    node.setAttribute('lang', content.lang);
    nextLanguage = content.lang;
  }
  if (content.data) {
    for (const [k, v] of Object.entries(content.data)) {
      const isCJK = /^[\u3000-\u9FFF\uF900-\uFAFF]/.test(k);
      node.setAttribute(`data-sc${'$'}{isCJK ? '' : '-'}${'$'}{toKebabCase(k)}`, v);
    }
  }
  if (content.style && typeof content.style === 'object') {
    setStructuredContentElementStyle(node, content.style);
  }
  renderStructuredContent(node, content.content ?? content.children ?? content.data?.content ?? '', nextLanguage, dictionary, exporting);
  if (content.colSpan) node.setAttribute('colspan', content.colSpan);
  if (content.rowSpan) node.setAttribute('rowspan', content.rowSpan);
  if (tag === 'table') {
    parent.appendChild(el('div', { className: 'gloss-sc-table-container' }, [node]));
    return;
  }
  parent.appendChild(node);
}
function createTags(entry) {
  const row = el('div', { className: 'tag-row' });
  if (entry.deinflectionTrace?.length) entry.deinflectionTrace.forEach(step => row.appendChild(el('span', { className: 'deinflection-tag', textContent: step.name })));
  if (window.showExpressionTags) {
    const tags = new Set();
    entry.glossaries?.forEach(g => parseTags(g.termTags).forEach(t => tags.add(t)));
    tags.forEach(t => row.appendChild(el('span', { className: 'expr-tag', textContent: t })));
  }
  if (entry.frequencies?.length) {
    if (window.harmonicFrequency) {
      const normalRow = el('span', { className: 'tag-row', styleText: 'display:none' });
      entry.frequencies.forEach(group => normalRow.appendChild(createFrequencyGroup(group)));
      const harmonic = createHarmonicFrequencyTag(entry.frequencies);
      const toggle = () => {
        const showNormal = normalRow.style.display === 'none';
        normalRow.style.display = showNormal ? '' : 'none';
        harmonic.style.display = showNormal ? 'none' : '';
      };
      normalRow.addEventListener('click', toggle);
      harmonic.addEventListener('click', toggle);
      row.appendChild(harmonic);
      row.appendChild(normalRow);
    } else {
      entry.frequencies.forEach(group => row.appendChild(createFrequencyGroup(group)));
    }
  }
  if (entry.pitches?.length) {
    const pitchContainer = el('div', { className: 'pitch-list' });
    if (window.deduplicatePitchAccents) {
      const seen = new Set();
      entry.pitches.forEach(pitch => {
        const unique = pitch.pitchPositions.filter(pos => !seen.has(pos));
        if (unique.length > 0) {
          unique.forEach(pos => seen.add(pos));
          pitchContainer.appendChild(createPitchGroup({ dictionary: pitch.dictionary, pitchPositions: unique }, entry.reading));
        }
      });
    } else {
      entry.pitches.forEach(pitch => pitchContainer.appendChild(createPitchGroup(pitch, entry.reading)));
    }
    row.appendChild(pitchContainer);
  }
  return row.childNodes.length ? row : null;
}
function createGlossaryTags(tags) {
  const filtered = (tags || []).filter(Boolean);
  if (!filtered.length) return null;
  return el('div', { className: 'tag-row' }, filtered.map(tag => el('span', { className: 'expr-tag', textContent: tag })));
}
function getFrequencyHarmonicRank(frequencies) {
  if (!frequencies || frequencies.length === 0) return DEFAULT_HARMONIC_RANK;
  const values = [];
  const seenDictionaries = new Set();
  frequencies.forEach(group => {
    const dictionary = group?.dictionary;
    if (dictionary && seenDictionaries.has(dictionary)) return;
    if (dictionary) seenDictionaries.add(dictionary);
    const firstFreq = group?.frequencies?.[0];
    if (!firstFreq) return;
    const displayValue = firstFreq.displayValue;
    if (displayValue != null) {
      const match = String(displayValue).match(/^\d+/);
      if (match) {
        const parsed = Number.parseInt(match[0], 10);
        if (parsed > 0) {
          values.push(parsed);
          return;
        }
      }
    }
    const value = firstFreq.value;
    if (value && value > 0) values.push(value);
  });
  if (values.length === 0) return DEFAULT_HARMONIC_RANK;
  const sumOfReciprocals = values.reduce((sum, value) => sum + (1 / value), 0);
  return String(Math.floor(values.length / sumOfReciprocals));
}
function createFrequencyGroup(group) {
  const values = (group.frequencies || []).map(freq => freq.displayValue || freq.value).join(', ');
  return el('span', { className: 'frequency-group', title: group.dictionary }, [
    el('span', { className: 'frequency-dict-label', textContent: group.dictionary }),
    el('span', { className: 'frequency-values', textContent: values })
  ]);
}
function isMoraPitchHigh(moraIndex, pitchAccentValue) {
  switch (pitchAccentValue) {
    case 0: return (moraIndex > 0);
    case 1: return (moraIndex < 1);
    default: return (moraIndex > 0 && moraIndex < pitchAccentValue);
  }
}
function getKanaMorae(text) {
  const morae = [];
  let i;
  for (const c of text) {
    if (SMALL_KANA_SET.has(c) && (i = morae.length) > 0) {
      morae[i - 1] += c;
    } else {
      morae.push(c);
    }
  }
  return morae;
}
function getPitchCategory(reading, pitchAccentValue, verbOrAdjective) {
  if (pitchAccentValue === 0) return 'heiban';
  if (verbOrAdjective) return pitchAccentValue > 0 ? 'kifuku' : null;
  if (pitchAccentValue === 1) return 'atamadaka';
  if (pitchAccentValue > 1) {
    const moraCount = getKanaMorae(reading).length;
    return pitchAccentValue >= moraCount ? 'odaka' : 'nakadaka';
  }
  return null;
}
function createPitchHtml(reading, pitchValue) {
  const morae = getKanaMorae(reading);
  const container = el('span', { className: 'pronunciation-text' });
  for (let i = 0; i < morae.length; i++) {
    const mora = morae[i];
    const isHigh = isMoraPitchHigh(i, pitchValue);
    const isHighNext = isMoraPitchHigh(i + 1, pitchValue);
    const moraSpan = el('span', {
      className: 'pronunciation-mora',
      'data-pitch': isHigh ? 'high' : 'low',
      'data-pitch-next': isHighNext ? 'high' : 'low',
      textContent: mora
    });
    moraSpan.appendChild(el('span', { className: 'pronunciation-mora-line' }));
    container.appendChild(moraSpan);
  }
  return container;
}
function createPitchGroup(pitchData, reading) {
  const container = el('div', { className: 'pitch-group' });
  container.appendChild(el('span', { className: 'pitch-dict-label', textContent: pitchData.dictionary }));
  const list = el('ul', { className: 'pitch-entries' });
  pitchData.pitchPositions.forEach((pitch) => {
    const li = el('li');
    li.appendChild(createPitchHtml(reading, pitch));
    li.appendChild(document.createTextNode(` [${'$'}{pitch}]`));
    list.appendChild(li);
  });
  container.appendChild(list);
  return container;
}
function createHarmonicFrequencyTag(frequencies) {
  return el('span', { className: 'frequency-group harmonic-frequency', title: 'Average frequency rank' }, [
    el('span', { className: 'frequency-dict-label', textContent: 'Average' }),
    el('span', { className: 'frequency-values', textContent: getFrequencyHarmonicRank(frequencies) })
  ]);
}
async function fetchAudioUrl(expression, reading) {
  const templates = window.audioSources;
  if (!templates?.length) return null;
  for (const template of templates) {
    const url = template
      .replace('{term}', encodeURIComponent(expression))
      .replace('{reading}', encodeURIComponent(reading || expression));
    try {
      const data = AndroidHoshi.fetchAudioAsync
        ? await fetchAudioSourceList(url)
        : AndroidHoshi.fetchAudioJson
          ? JSON.parse(AndroidHoshi.fetchAudioJson(url))
          : await fetch(`audio://?url=${'$'}{encodeURIComponent(url)}`).then(response => response.json());
      if (data.type === 'audioSourceList' && data.audioSources?.[0]?.url) return data.audioSources[0].url;
    } catch {}
  }
  return null;
}
const pendingAudioFetches = new Map();
function fetchAudioSourceList(url) {
  return new Promise((resolve, reject) => {
    const requestId = `${'$'}{Date.now()}:${'$'}{Math.random().toString(36).slice(2)}`;
    const timeoutId = setTimeout(() => {
      pendingAudioFetches.delete(requestId);
      reject(new Error('audio fetch timeout'));
    }, 22000);
    pendingAudioFetches.set(requestId, payload => {
      clearTimeout(timeoutId);
      pendingAudioFetches.delete(requestId);
      try {
        resolve(JSON.parse(payload));
      } catch (error) {
        reject(error);
      }
    });
    try {
      AndroidHoshi.fetchAudioAsync(requestId, url);
    } catch (error) {
      clearTimeout(timeoutId);
      pendingAudioFetches.delete(requestId);
      reject(error);
    }
  });
}
window.__moriResolveAudioFetch = function(requestId, payload) {
  const resolver = pendingAudioFetches.get(requestId);
  if (resolver) resolver(payload);
};
function playWordAudio(audioUrl) {
  try {
    AndroidHoshi.playWordAudio(JSON.stringify({ url: audioUrl, mode: window.audioPlaybackMode || 'interrupt' }));
    return true;
  } catch {
    return false;
  }
}
function showAudioError(button) {
  button.disabled = false;
  button.textContent = 'x';
  setTimeout(() => { button.textContent = '♪'; }, 1500);
}
function createAudioButton(expression, reading, entryIndex) {
  const audioKey = `${'$'}{entryIndex}:${'$'}{expression}:${'$'}{reading || expression}`;
  const button = el('button', {
    className: 'audio-button',
    textContent: '♪',
    title: ${WebJson.encodeToString(audioButtonTitle)},
    onclick: async () => {
      if (button.disabled) return;
      button.disabled = true;
      button.textContent = '…';
      try {
        if (!audioUrls[audioKey]) audioUrls[audioKey] = await fetchAudioUrl(expression, reading || expression);
      } catch {
        showAudioError(button);
        return;
      }
      if (!audioUrls[audioKey]) {
        showAudioError(button);
        return;
      }
      button.disabled = false;
      button.textContent = '♪';
      if (!playWordAudio(audioUrls[audioKey])) showAudioError(button);
    }
  });
  return button;
}
function constructFuriganaPlain(expression, reading) {
  if (!reading || reading === expression) return expression;
  return segmentFurigana(expression, reading)
    .map(([text, furigana]) => furigana ? `${'$'}{text}[${'$'}{furigana}]` : `${'$'}{text} `)
    .join('')
    .trim();
}
function applyTableStyles(html) {
  const tableStyle = 'table-layout:auto;border-collapse:collapse;';
  const cellStyle = 'border-style:solid;padding:0.25em;vertical-align:top;border-width:1px;border-color:currentColor;';
  const thStyle = 'font-weight:bold;' + cellStyle;
  return html
    .replace(/<table(?=[>\s])/g, `<table style="${'$'}{tableStyle}"`)
    .replace(/<th(?=[>\s])/g, `<th style="${'$'}{thStyle}"`)
    .replace(/<td(?=[>\s])/g, `<td style="${'$'}{cellStyle}"`);
}
function applyImageStyles(node, imageContainer, aspectRatioSizer, imageBackground, image, filename, appearance, useEmUnits) {
  node.style.cssText += 'display:inline-block;position:relative;line-height:1;max-width:100%;';
  imageContainer.style.cssText += `display:inline-block;white-space:nowrap;max-width:100%;max-height:100vh;position:relative;vertical-align:top;line-height:0;overflow:hidden;font-size:${'$'}{useEmUnits ? '1em' : '1px'};`;
  aspectRatioSizer.style.cssText += 'display:inline-block;width:0;vertical-align:top;font-size:0;';
  image.style.cssText += 'display:inline-block;vertical-align:top;object-fit:contain;border:none;outline:none;position:absolute;left:0;top:0;width:100%;height:100%;';
  if (appearance === 'monochrome') {
    imageBackground.style.cssText += `--image:url("${'$'}{filename}");position:absolute;left:0;top:0;width:100%;height:100%;-webkit-mask-repeat:no-repeat;-webkit-mask-position:center center;-webkit-mask-mode:alpha;-webkit-mask-size:contain;-webkit-mask-image:var(--image);mask-repeat:no-repeat;mask-position:center center;mask-mode:alpha;mask-size:contain;mask-image:var(--image);background-color:currentColor;`;
    image.style.opacity = '0';
  }
}
function scopedDictionaryStyle(dictName, css) {
  if (!css) return '';
  const scopedCss = constructDictCss(css, dictName, '.yomitan-glossary');
  return scopedCss
    .replace(/\s+/g, ' ')
    .replace(/\s*\{\s*/g, ' { ')
    .replace(/\s*\}\s*/g, ' }\n')
    .replace(/;\s*/g, '; ')
    .trim();
}
function renderGlossaryContent(g, exporting = false) {
  const tempDiv = document.createElement('div');
  try {
    renderStructuredContent(tempDiv, JSON.parse(g.content), null, g.dictionary || '', exporting);
  } catch {
    renderStructuredContent(tempDiv, g.content, null, g.dictionary || '', exporting);
  }
  return applyTableStyles(tempDiv.innerHTML);
}
function constructSingleGlossaryHtml(entryIndex) {
  if (!window.lookupEntries || entryIndex >= window.lookupEntries.length) return {};
  const entry = window.lookupEntries[entryIndex];
  const glossaries = {};
  let lastDict = null;
  let currentGlossary = '';
  let prevTags = null;
  const flush = () => {
    if (!lastDict) return;
    let html = `<div style="text-align: left;" class="yomitan-glossary"><ol>${'$'}{currentGlossary}</ol>`;
    const css = window.dictionaryStyles?.[lastDict] ?? '';
    if (css) html += `<style>${'$'}{scopedDictionaryStyle(lastDict, css)}</style>`;
    if (window.compactGlossariesAnki) html += `<style>${'$'}{COMPACT_GLOSSARIES_ANKI}</style>`;
    html += `</div>`;
    glossaries[lastDict] = html;
    currentGlossary = '';
  };
  (entry.glossaries || []).forEach(g => {
    const dictName = g.dictionary || '';
    const dictChanged = lastDict !== dictName;
    if (dictChanged) {
      flush();
      lastDict = dictName;
      prevTags = null;
    }
    const parsedTags = parseTags(g.definitionTags).filter(tag => !NUMERIC_TAG.test(tag));
    const posTags = [...new Set(parsedTags.filter(isPartOfSpeech))].sort();
    const currentTags = JSON.stringify(posTags);
    const filteredTags = parsedTags.filter(tag => !isPartOfSpeech(tag) || !(prevTags !== null && prevTags === currentTags));
    const tags = filteredTags.length > 0 ? filteredTags.join(', ') : '';
    const label = dictChanged ? (tags ? `(${'$'}{tags}, ${'$'}{dictName})` : `(${'$'}{dictName})`) : (tags ? `(${'$'}{tags})` : '');
    currentGlossary += `<li data-dictionary="${'$'}{dictName}"><i>${'$'}{label}</i> <span>${'$'}{renderGlossaryContent(g, true)}</span></li>`;
    prevTags = currentTags;
  });
  flush();
  return glossaries;
}
function constructGlossaryHtml(entryIndex) {
  if (!window.lookupEntries || entryIndex >= window.lookupEntries.length) return '';
  const entry = window.lookupEntries[entryIndex];
  let glossaryItems = '';
  const styles = {};
  let lastDict = '';
  let prevTags = null;
  let index = 0;
  (entry.glossaries || []).forEach(g => {
    const dictName = g.dictionary || '';
    index++;
    let label = '';
    const parsedTags = parseTags(g.definitionTags).filter(tag => !NUMERIC_TAG.test(tag));
    const posTags = [...new Set(parsedTags.filter(isPartOfSpeech))].sort();
    const currentTags = JSON.stringify(posTags);
    const filteredTags = parsedTags.filter(tag => !isPartOfSpeech(tag) || !(prevTags !== null && prevTags === currentTags));
    const tags = filteredTags.length > 0 ? filteredTags.join(', ') : '';
    if (dictName !== lastDict) {
      index = 1;
      lastDict = dictName;
      label = tags ? `(${'$'}{index}, ${'$'}{tags}, ${'$'}{dictName})` : `(${'$'}{index}, ${'$'}{dictName})`;
    } else {
      label = tags ? `(${'$'}{index}, ${'$'}{tags})` : `(${'$'}{index})`;
    }
    glossaryItems += `<li data-dictionary="${'$'}{dictName}"><i>${'$'}{label}</i> <span>${'$'}{renderGlossaryContent(g, true)}</span></li>`;
    prevTags = currentTags;
    const css = window.dictionaryStyles?.[dictName];
    if (css && !styles[dictName]) styles[dictName] = css;
  });
  let result = '<div style="text-align: left;" class="yomitan-glossary"><ol>';
  result += glossaryItems;
  result += '</ol>';
  for (const [dictName, css] of Object.entries(styles)) {
    result += `<style>${'$'}{scopedDictionaryStyle(dictName, css)}</style>`;
  }
  if (window.compactGlossariesAnki) result += `<style>${'$'}{COMPACT_GLOSSARIES_ANKI}</style>`;
  result += '</div>';
  return result;
}
function constructFrequencyHtml(frequencies) {
  if (!frequencies || frequencies.length === 0) return '';
  let result = '<ul style="text-align: left;">';
  frequencies.forEach(group => {
    if (!group?.frequencies?.length) return;
    const dictName = group.dictionary || '';
    group.frequencies.forEach(freq => {
      result += `<li>${'$'}{dictName}: ${'$'}{freq.displayValue || freq.value}</li>`;
    });
  });
  result += '</ul>';
  return result;
}
function constructPitchPositionHtml(pitches) {
  if (!pitches?.length) return '';
  let result = '<ol>';
  pitches.forEach(group => {
    (group.pitchPositions || []).forEach(pos => {
      result += `<li><span style="display:inline;"><span>[</span><span>${'$'}{pos}</span><span>]</span></span></li>`;
    });
  });
  result += '</ol>';
  return result;
}
function buildMiningPayload(entry, entryIndex, popupSelectionText) {
  const audioUrl = audioUrls[`${'$'}{entryIndex}:${'$'}{entry.expression}:${'$'}{entry.reading || entry.expression}`];
  currentDictionaryMedia = new Map();
  const glossary = constructGlossaryHtml(entryIndex);
  const singleGlossaries = constructSingleGlossaryHtml(entryIndex);
  const dictionaryMedia = currentDictionaryMedia;
  currentDictionaryMedia = null;
  const selectedDictionary = selectedDictionaries[entryIndex]?.name || '';
  return {
    expression: entry.expression || '',
    reading: entry.reading || '',
    matched: entry.matched || entry.expression || '',
    furiganaPlain: constructFuriganaPlain(entry.expression || '', entry.reading || ''),
    frequenciesHtml: constructFrequencyHtml(entry.frequencies),
    freqHarmonicRank: getFrequencyHarmonicRank(entry.frequencies),
    glossary,
    glossaryFirst: Object.values(singleGlossaries)[0] || '',
    singleGlossaries,
    pitchPositions: constructPitchPositionHtml(entry.pitches),
    pitchCategories: pitchCategories(entry).replace(/\s+/g, ''),
    selectedDictionary,
    popupSelectionText: popupSelectionText || '',
    audio: audioUrl || '',
    dictionaryMedia: Array.from(dictionaryMedia.values())
  };
}
function createMineButton(entry, entryIndex) {
  const button = el('button', {
    className: 'mine-button',
    textContent: '+',
    title: 'Add to Anki',
    ontouchstart: () => {
      window.lastPopupSelectionText = window.getSelection()?.toString() || '';
    },
    onclick: async () => {
      if (button.disabled && !window.allowDupes) return;
      button.disabled = true;
      button.textContent = '…';
      try {
        if (window.needsAudio && !audioUrls[`${'$'}{entryIndex}:${'$'}{entry.expression}:${'$'}{entry.reading || entry.expression}`] && window.audioSources?.length) {
          audioUrls[`${'$'}{entryIndex}:${'$'}{entry.expression}:${'$'}{entry.reading || entry.expression}`] = await fetchAudioUrl(entry.expression, entry.reading || entry.expression);
        }
        const payload = buildMiningPayload(entry, entryIndex, window.lastPopupSelectionText || '');
        AndroidHoshi.mineEntry(JSON.stringify(payload));
        AndroidHoshi.checkDuplicate?.(entry.expression || '');
        button.textContent = '+';
        button.disabled = false;
      } catch {
        button.textContent = '+';
        button.disabled = false;
      }
    }
  });
  if (window.ankiDuplicateExpression && window.ankiDuplicateExpression === entry.expression) {
    button.textContent = '✓';
    button.classList.add('duplicate');
    button.disabled = !window.allowDupes;
  } else {
    try { AndroidHoshi.checkDuplicate?.(entry.expression || ''); } catch {}
  }
  return button;
}
function pitchCategories(entry) {
  const tags = parseTags((entry.glossaries || []).map(g => g.termTags).join(' '));
  const verbOrAdjective = tags.some(isPartOfSpeech);
  const categories = new Set();
  (entry.pitches || []).forEach(group => (group.pitchPositions || []).forEach(position => {
    const category = getPitchCategory(entry.reading || entry.expression, position, verbOrAdjective);
    if (category) categories.add(category);
  }));
  return Array.from(categories).join(', ');
}
function createEntry(entry, index) {
  const entryDiv = el('div', { className: 'entry' });
  const expression = el('span', { className: 'expression' });
  buildFurigana(expression, entry.expression, entry.reading);
  const headerChildren = [expression];
  const buttons = [];
  if (window.audioSources?.length) buttons.push(createAudioButton(entry.expression, entry.reading, index));
  buttons.push(createMineButton(entry, index));
  if (buttons.length) {
    headerChildren.push(el('div', { className: 'header-buttons' }, buttons));
  }
  entryDiv.appendChild(el('div', { className: 'entry-header' }, headerChildren));
  const tags = createTags(entry);
  if (tags) entryDiv.appendChild(tags);
  const grouped = {};
  entry.glossaries?.forEach(g => (grouped[g.dictionary] ??= []).push(g));
  Object.entries(grouped).forEach(([dictName, items], dictIndex) => {
    const details = el('details', { className: 'glossary-group' });
    details.open = !window.collapseDictionaries || dictIndex === 0;
    const summary = el('summary', { textContent: dictName });
    let timer = null;
    let longPressed = false;
    const toggleSelection = () => {
      longPressed = true;
      const selected = selectedDictionaries[index];
      selected?.label.classList.remove('selected');
      if (selected?.name === dictName) {
        delete selectedDictionaries[index];
      } else {
        selectedDictionaries[index] = { name: dictName, label: summary };
        summary.classList.add('selected');
      }
    };
    summary.addEventListener('pointerdown', () => {
      longPressed = false;
      timer = setTimeout(toggleSelection, 400);
    });
    const cancel = () => { clearTimeout(timer); };
    summary.addEventListener('pointerup', cancel);
    summary.addEventListener('pointercancel', cancel);
    summary.addEventListener('click', e => { if (longPressed) e.preventDefault(); });
    details.appendChild(summary);
    const wrapper = el('div', { className: window.compactGlossaries ? 'compact' : '' });
    wrapper.setAttribute('data-dictionary', dictName);
    const dictStyle = window.dictionaryStyles?.[dictName] ?? '';
    wrapper.appendChild(el('style', {
      textContent: `
        [data-dictionary="${'$'}{dictName}"] {
          @media (prefers-color-scheme: light) { color: #000; }
          @media (prefers-color-scheme: dark) { color: #fff; }
          ${'$'}{dictStyle}
        }
      `.trim()
    }));
    const termTags = [...new Set(parseTags(items[0]?.termTags))];
    const termTagsRow = createGlossaryTags(termTags);
    if (termTagsRow) wrapper.appendChild(termTagsRow);
    const renderContent = (parent, content) => {
      try { renderStructuredContent(parent, JSON.parse(content), null, dictName); } catch { renderStructuredContent(parent, content, null, dictName); }
    };
    if (items.length > 1) {
      const ol = el('ol');
      let prevTags = null;
      items.forEach(item => {
        const li = el('li');
        const parsedTags = parseTags(item.definitionTags).filter(tag => !NUMERIC_TAG.test(tag));
        const posTags = [...new Set(parsedTags.filter(isPartOfSpeech))].sort();
        const currentTags = JSON.stringify(posTags);
        const filteredTags = parsedTags.filter(tag => !isPartOfSpeech(tag) || !(prevTags !== null && prevTags === currentTags));
        const tags = createGlossaryTags(filteredTags);
        if (tags) li.appendChild(tags);
        const content = el('div', { className: 'glossary-content' });
        renderContent(content, item.content);
        li.appendChild(content);
        ol.appendChild(li);
        prevTags = currentTags;
      });
      wrapper.appendChild(ol);
    } else {
      items.forEach(item => {
        const itemWrapper = el('div');
        const tags = createGlossaryTags(parseTags(item.definitionTags).filter(tag => !NUMERIC_TAG.test(tag)));
        if (tags) itemWrapper.appendChild(tags);
        const content = el('div', { className: 'glossary-content' });
        renderContent(content, item.content);
        itemWrapper.appendChild(content);
        wrapper.appendChild(itemWrapper);
      });
    }
    details.appendChild(wrapper);
    entryDiv.appendChild(details);
  });
  return entryDiv;
}
function renderEntries(entries, target) {
  target.innerHTML = '';
  entries.forEach((entry, index) => {
    if (index > 0) target.appendChild(el('hr'));
    const entryDiv = createEntry(entry, index);
    target.appendChild(entryDiv);
    if (window.audioEnableAutoplay && window.audioSources?.length && index === 0) {
      const autoplayKey = `${'$'}{window.lookupQuery || ''}:${'$'}{entry.expression}:${'$'}{entry.reading || ''}:${'$'}{entry.matched || ''}`;
      if (!AndroidHoshi.consumeAutoplay || AndroidHoshi.consumeAutoplay(autoplayKey)) {
        setTimeout(() => entryDiv.querySelector('.audio-button')?.click(), 70);
      }
    }
  });
}
function snapshot() {
  const container = document.getElementById('entries-container');
  return {
    nodes: [...container.childNodes],
    scrollTop: document.scrollingElement.scrollTop,
    lookupEntries: window.lookupEntries,
    dictionaryStyles: window.dictionaryStyles,
    lookupQuery: window.lookupQuery,
    emptyMessage: document.getElementById('empty-state').textContent || ''
  };
}
function restore(state) {
  const container = document.getElementById('entries-container');
  const emptyState = document.getElementById('empty-state');
  container.replaceChildren(...state.nodes);
  window.lookupEntries = state.lookupEntries;
  window.dictionaryStyles = state.dictionaryStyles;
  window.lookupQuery = state.lookupQuery;
  emptyState.textContent = state.emptyMessage || '';
  audioUrls = {};
  selectedDictionaries = {};
  normalizeRubyTextContainers(container);
  requestAnimationFrame(() => {
    document.scrollingElement.scrollTop = state.scrollTop;
  });
  notifyNavigationState();
}
function redirectToLookup(result, query) {
  if (!result?.entries?.length) return false;
  backStack.push(snapshot());
  forwardStack.length = 0;
  audioUrls = {};
  selectedDictionaries = {};
  window.lookupEntries = result.entries;
  window.dictionaryStyles = result.styles || {};
  window.lookupQuery = query || '';
  const container = document.getElementById('entries-container');
  const emptyState = document.getElementById('empty-state');
  emptyState.textContent = '';
  renderEntries(window.lookupEntries, container);
  normalizeRubyTextContainers(container);
  requestAnimationFrame(() => {
    document.scrollingElement.scrollTop = 0;
    requestAnimationFrame(() => {
      document.scrollingElement.scrollTop = 0;
    });
  });
  notifyNavigationState();
  return true;
}
function navigate(from, to) {
  if (!from.length) return false;
  to.push(snapshot());
  restore(from.pop());
  return true;
}
window.navigateBack = () => navigate(backStack, forwardStack);
window.navigateForward = () => navigate(forwardStack, backStack);
window.hoshiSelection = {
    selection: null,
    scanDelimiters: '。、！？…‥「」『』（）()【】〈〉《》〔〕｛｝{}［］[]・：；:;，,.─\n\r',
    sentenceDelimiters: '。！？.!?\n\r',
    trailingSentenceChars: '。、！？…‥」』）)】〉》〕｝}］]',
    brackets: {'「':'」', '『': '』', '（':'）', '(':')', '【':'】', '〈':'〉', '《':'》', '〔':'〕', '｛':'｝', '{':'}', '［':'］', '[':']'},

    isScanBoundary(char) {
        return /^[\s\u3000]$/.test(char) || this.scanDelimiters.includes(char);
    },

    isFurigana(node) {
        const el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
        return !!el?.closest('rt, rp');
    },

    findParagraph(node) {
        let el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
        return el?.closest('p, .glossary-content') || null;
    },

    createWalker(rootNode) {
        const root = rootNode || document.body;
        return document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
            acceptNode: (n) => this.isFurigana(n) ? NodeFilter.FILTER_REJECT : NodeFilter.FILTER_ACCEPT
        });
    },

    inCharRange(charRange, x, y) {
        const rects = charRange.getClientRects();
        if (rects.length) {
            for (const rect of rects) {
                if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                    return true;
                }
            }
            return false;
        }
        const rect = charRange.getBoundingClientRect();
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    },

    getCaretRange(x, y) {
        if (document.caretPositionFromPoint) {
            const pos = document.caretPositionFromPoint(x, y);
            if (!pos) return null;
            const range = document.createRange();
            range.setStart(pos.offsetNode, pos.offset);
            range.collapse(true);
            return range;
        } else {
            const element = document.elementFromPoint(x, y);
            if (!element) return null;

            const container = element.closest('p, div, span, ruby, a') || document.body;
            const walker = this.createWalker(container);

            const range = document.createRange();
            let node;
            while (node = walker.nextNode()) {
                for (let i = 0; i < node.textContent.length; i++) {
                    range.setStart(node, i);
                    range.setEnd(node, i + 1);
                    if (this.inCharRange(range, x, y)) {
                        range.collapse(true);
                        return range;
                    }
                }
            }
            return document.caretRangeFromPoint(x, y);
        }
    },

    getCharacterAtPoint(x, y) {
        const range = this.getCaretRange(x, y);
        if (!range) return null;

        const node = range.startContainer;
        if (node.nodeType !== Node.TEXT_NODE) return null;
        if (this.isFurigana(node)) return null;

        const text = node.textContent;
        const caret = range.startOffset;

        for (const offset of [caret, caret - 1, caret + 1]) {
            if (offset < 0 || offset >= text.length) continue;

            const charRange = document.createRange();
            charRange.setStart(node, offset);
            charRange.setEnd(node, offset + 1);
            if (this.inCharRange(charRange, x, y)) {
                if (this.isScanBoundary(text[offset])) return null;
                return { node, offset };
            }
        }

        return null;
    },

    getSentence(startNode, startOffset) {
        const container = this.findParagraph(startNode) || document.body;
        const walker = this.createWalker(container);

        walker.currentNode = startNode;
        const partsBefore = [];
        let node = startNode;
        let limit = startOffset;

        while (node) {
            const text = node.textContent;
            let foundStart = false;
            for (let i = limit - 1; i >= 0; i--) {
                if (this.sentenceDelimiters.includes(text[i])) {
                    partsBefore.push(text.slice(i + 1, limit));
                    foundStart = true;
                    break;
                }
            }

            if (foundStart) break;

            partsBefore.push(text.slice(0, limit));
            node = walker.previousNode();
            if (node) limit = node.textContent.length;
        }

        walker.currentNode = startNode;
        const partsAfter = [];
        node = startNode;
        let start = startOffset;

        while (node) {
            const text = node.textContent;
            let foundEnd = false;

            for (let i = start; i < text.length; i++) {
                if (this.sentenceDelimiters.includes(text[i])) {
                    let end = i + 1;
                    while (end < text.length) {
                        if (!this.trailingSentenceChars.includes(text[end])) break;
                        end += 1;
                    }
                    partsAfter.push(text.slice(start, end));
                    foundEnd = true;
                    break;
                }
            }

            if (foundEnd) break;

            partsAfter.push(text.slice(start));
            node = walker.nextNode();
            start = 0;
        }

        let sentence = (partsBefore.reverse().join('') + partsAfter.join('')).trim();
        const closeBrackets = new Set(Object.values(this.brackets));
        const openBrackets = new Set(Object.keys(this.brackets));
        let stack = [];
        let unmatchedClose = [];

        for (let i = 0; i < sentence.length; i++) {
            const ch = sentence[i];
            if (openBrackets.has(ch)) {
                stack.push(ch);
            } else if (closeBrackets.has(ch)) {
                if (stack.length > 0 && this.brackets[stack[stack.length - 1]] === ch) {
                    stack.pop();
                } else {
                    unmatchedClose.push(ch);
                }
            }
        }

        let startSlice = 0;
        while (stack.length > 0 && startSlice < sentence.length - 1) {
            if (stack[0] === sentence[startSlice]) {
                stack.shift();
            } else break;
            startSlice++;
        }

        let endSlice = sentence.length - 1;
        let endIdx = sentence.length - 1;
        while (unmatchedClose.length > 0 && endIdx > startSlice) {
            if (unmatchedClose[unmatchedClose.length - 1] === sentence[endIdx]) {
                unmatchedClose.pop();
                endSlice = endIdx - 1;
            } else if (!this.sentenceDelimiters.includes(sentence[endIdx])) break;
            endIdx--;
        }
        return sentence.slice(startSlice, endSlice + 1).trim();
    },

    selectText(x, y, maxLength) {
        const hit = this.getCharacterAtPoint(x, y);
        if (!hit) {
            this.clearSelection();
            return null;
        }

        if (this.selection &&
            hit.node === this.selection.startNode &&
            hit.offset === this.selection.startOffset) {
            this.clearSelection();
            return null;
        }

        this.clearSelection();

        const container = this.findParagraph(hit.node) || document.body;
        const walker = this.createWalker(container);

        let text = '';
        let node = hit.node;
        let offset = hit.offset;
        let ranges = [];

        walker.currentNode = node;
        while (text.length < maxLength && node) {
            const content = node.textContent;
            const start = offset;

            while (offset < content.length && text.length < maxLength) {
                const char = content[offset];
                if (this.isScanBoundary(char)) break;
                text += char;
                offset++;
            }

            if (offset > start) {
                ranges.push({ node, start, end: offset });
            }

            if (offset < content.length || text.length >= maxLength) break;

            node = walker.nextNode();
            offset = 0;
        }

        if (!text) return null;

        this.selection = {
            startNode: hit.node,
            startOffset: hit.offset,
            ranges,
            text
        };

        return {
            text,
            sentence: this.getSentence(hit.node, hit.offset),
            rect: this.getSelectionRect(x, y)
        };
    },

    getSelectionRect(x, y) {
        if (!this.selection?.ranges.length) return null;

        const first = this.selection.ranges[0];
        const range = document.createRange();
        range.setStart(first.node, first.start);
        range.setEnd(first.node, first.start + 1);

        const rects = Array.from(range.getClientRects());
        const rect = rects.find(rect => x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) ?? range.getBoundingClientRect();
        return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
    },

    highlightSelection(charCount) {
        if (!this.selection?.ranges.length) return;

        const highlights = [];
        let remaining = charCount;

        for (const r of this.selection.ranges) {
            if (remaining <= 0) break;

            let end = r.start;
            while (end < r.end && remaining > 0) {
                const char = String.fromCodePoint(r.node.textContent.codePointAt(end));
                end += char.length;
                remaining--;
            }

            const range = document.createRange();
            range.setStart(r.node, r.start);
            range.setEnd(r.node, end);
            highlights.push(range);
        }

        CSS.highlights?.set('hoshi-selection', new Highlight(...highlights));
    },

    clearSelection() {
        window.getSelection()?.removeAllRanges();
        CSS.highlights?.get('hoshi-selection')?.clear();
        this.selection = null;
    }
};
function hidePopup() {
  document.getElementById('popup').hidden = true;
  document.getElementById('popup-backdrop').hidden = true;
  window.hoshiSelection.clearSelection();
}
function showPopup(selection) {
  window.lastPopupSelectionText = selection.text || '';
  const result = JSON.parse(AndroidHoshi.lookup(selection.text, window.maxResults || 16));
  if (!result.entries?.length) { hidePopup(); return; }
  const matched = result.entries[0]?.matched || '';
  if (matched) window.hoshiSelection.highlightSelection(Array.from(matched).length);
  const popup = document.getElementById('popup');
  const content = document.getElementById('popup-content');
  const oldEntries = window.lookupEntries;
  const oldStyles = window.dictionaryStyles;
  window.lookupEntries = result.entries;
  window.dictionaryStyles = result.styles || {};
  renderEntries(result.entries, content);
  normalizeRubyTextContainers(content);
  window.lookupEntries = oldEntries;
  window.dictionaryStyles = oldStyles;
  const yBelow = selection.rect.bottom + 8;
  const desiredTop = yBelow + popup.offsetHeight > window.innerHeight ? Math.max(8, selection.rect.top - popup.offsetHeight - 8) : yBelow;
  popup.style.top = `${'$'}{desiredTop}px`;
  popup.hidden = false;
  document.getElementById('popup-backdrop').hidden = false;
}
function highlightLookupMatch(selection) {
  try {
    const result = JSON.parse(AndroidHoshi.lookup(selection.text, window.maxResults || 16));
    const matched = result.entries?.[0]?.matched || '';
    if (matched) window.hoshiSelection.highlightSelection(Array.from(matched).length);
  } catch {}
}
function normalizeRubyTextContainers(root) {
  root.querySelectorAll('.glossary-content ruby').forEach(ruby => {
    ruby.childNodes.forEach(node => {
      if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
        const span = document.createElement('span');
        span.textContent = node.textContent;
        node.replaceWith(span);
      }
    });
  });
}
document.getElementById('popup-backdrop').addEventListener('click', hidePopup);
if (window.swipeThreshold) {
  let swipeStartX = 0;
  let swipeStartY = 0;
  document.addEventListener('touchstart', e => {
    swipeStartX = e.touches[0]?.clientX ?? 0;
    swipeStartY = e.touches[0]?.clientY ?? 0;
  });
  document.addEventListener('touchend', e => {
    const touch = e.changedTouches[0];
    if (!touch) return;
    const dx = touch.clientX - swipeStartX;
    const dy = touch.clientY - swipeStartY;
    const hasSelection = window.getSelection().toString();
    if (Math.abs(dx) > window.swipeThreshold && Math.abs(dy) < 20 && !hasSelection) {
      try { AndroidHoshi.swipeDismiss(); } catch {}
    }
  });
}
document.addEventListener('click', e => {
  if (window.enableInternalPopup && e.target.closest('#popup')) return;
  const target = e.target?.nodeType === Node.TEXT_NODE ? e.target.parentElement : e.target;
  if (target?.closest('.expression')) {
    if (window.enableInternalPopup) hidePopup();
    return;
  }
  if (!target?.closest('.glossary-content') && !target?.closest('.expr-tag')) {
    if (window.enableInternalPopup) hidePopup();
    return;
  }
  const selection = window.hoshiSelection.selectText(e.clientX, e.clientY, window.scanLength || 16);
  if (selection) {
    if (window.enableInternalPopup) {
      showPopup(selection);
    } else {
      highlightLookupMatch(selection);
      try { AndroidHoshi.openChildPopup(JSON.stringify(selection)); } catch {}
    }
  } else if (window.enableInternalPopup) {
    hidePopup();
  }
});
const container = document.getElementById('entries-container');
if (window.lookupEntries.length) {
  renderEntries(window.lookupEntries, container);
  normalizeRubyTextContainers(container);
} else {
  document.getElementById('empty-state').textContent = window.emptyMessage || '';
}
notifyNavigationState();
    """.trimIndent()
